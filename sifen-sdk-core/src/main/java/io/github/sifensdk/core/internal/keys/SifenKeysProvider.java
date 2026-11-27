/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.internal.keys;

import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.security.SignatureException;
import java.security.SignatureSpi;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/**
 * The provider that signs with {@link ExternalKey}s, and only with them (ADR-0012).
 *
 * <p>It keeps no state: each key carries what signs with it, so one provider serves every issuer.
 * XML signatures receive it through the signing context and need no registration. JSSE only uses
 * installed providers, so the client calls {@link #install()} before a TLS handshake with an
 * external key: it is added once, at the end of the list, and declares that it supports only {@link
 * ExternalKey}, so it never takes over any other key.
 */
public final class SifenKeysProvider extends Provider {

  /**
   * Name under which it is installed: unique per copy of the SDK (class loader), so two
   * applications in the same JVM each install their own, and each signs only its own keys.
   */
  public static final String NAME =
      "SifenSdkKeys-" + Integer.toHexString(System.identityHashCode(SifenKeysProvider.class));

  /** The single instance. Stateless, so sharing it is safe. */
  public static final SifenKeysProvider INSTANCE = new SifenKeysProvider();

  private static final long serialVersionUID = 1L;

  /** JCA name of RSASSA-PKCS1-v1_5 with SHA-256 (XMLDSig rsa-sha256; TLS rsa_pkcs1_sha256). */
  public static final String SHA256_WITH_RSA = "SHA256withRSA";

  /** JCA name of RSASSA-PSS (TLS 1.3 rsa_pss_rsae_sha256). */
  public static final String RSASSA_PSS = "RSASSA-PSS";

  private SifenKeysProvider() {
    super(NAME, "1.0", "SIFEN SDK: signatures with keys held in a token or by a remote signer");
    Map<String, String> attributes = Map.of("SupportedKeyClasses", ExternalKey.class.getName());
    for (String algorithm : new String[] {SHA256_WITH_RSA, RSASSA_PSS}) {
      putService(new SignatureService(this, algorithm, attributes));
    }
  }

  /**
   * Installs the provider at the end of the list, once. Needed only for TLS.
   *
   * @throws IllegalStateException if another provider is already installed under {@link #NAME} (for
   *     example a second copy of the SDK in another class loader)
   */
  public static synchronized void install() {
    Provider current = Security.getProvider(NAME);
    if (current == INSTANCE) {
      return;
    }
    if (current != null) {
      throw new IllegalStateException(
          "Another provider is installed as " + NAME + "; is the SDK loaded twice?");
    }
    Security.addProvider(INSTANCE);
  }

  private static final class SignatureService extends Service {

    SignatureService(Provider provider, String algorithm, Map<String, String> attributes) {
      super(provider, "Signature", algorithm, Spi.class.getName(), null, attributes);
    }

    @Override
    public Object newInstance(@Nullable Object constructorParameter) {
      return new Spi(getAlgorithm());
    }
  }

  /** Signs with an {@link ExternalKey}; never verifies. */
  private static final class Spi extends SignatureSpi {

    private final String algorithm;
    private @Nullable AlgorithmParameterSpec parameters;
    private @Nullable ExternalKey key;
    private ExternalKey.@Nullable Session session;

    Spi(String algorithm) {
      this.algorithm = algorithm;
    }

    @Override
    protected void engineInitSign(PrivateKey privateKey) throws InvalidKeyException {
      if (!(privateKey instanceof ExternalKey)) {
        throw new InvalidKeyException("Only keys held in a token or by a remote signer");
      }
      ExternalKey external = (ExternalKey) privateKey;
      if (!external.supports(algorithm)) {
        // Rejected here, not when signing, so that JSSE moves on to another signature scheme.
        throw new InvalidKeyException("The key's signer does not support " + algorithm);
      }
      this.key = external;
      this.session = null;
      // Opened now, so that a key the backend rejects fails here and JSSE can try another scheme.
      try {
        session();
      } catch (SignatureException e) {
        this.key = null;
        InvalidKeyException invalid = new InvalidKeyException("The key's signer cannot sign");
        invalid.initCause(e.getCause() != null ? e.getCause() : e);
        throw invalid;
      }
    }

    @Override
    protected void engineInitVerify(PublicKey publicKey) throws InvalidKeyException {
      throw new InvalidKeyException("This provider only signs");
    }

    @Override
    protected void engineSetParameter(@Nullable AlgorithmParameterSpec params)
        throws InvalidAlgorithmParameterException {
      if (params != null && !RSASSA_PSS.equals(algorithm)) {
        throw new InvalidAlgorithmParameterException(algorithm + " takes no parameters");
      }
      this.parameters = params;
      this.session = null;
      if (key != null) {
        try {
          session();
        } catch (SignatureException e) {
          throw new InvalidAlgorithmParameterException(
              "The key's signer rejects the parameters", e);
        }
      }
    }

    @Override
    protected @Nullable AlgorithmParameters engineGetParameters() {
      return null;
    }

    private ExternalKey.Session session() throws SignatureException {
      ExternalKey k = key;
      if (k == null) {
        throw new SignatureException("Not initialized for signing");
      }
      ExternalKey.Session s = session;
      if (s == null) {
        try {
          s = k.backend().open(algorithm, parameters);
        } catch (GeneralSecurityException | RuntimeException e) {
          throw new SignatureException("Could not start the signature", e);
        }
        session = s;
      }
      return s;
    }

    @Override
    protected void engineUpdate(byte b) throws SignatureException {
      engineUpdate(new byte[] {b}, 0, 1);
    }

    @Override
    protected void engineUpdate(byte[] b, int off, int len) throws SignatureException {
      try {
        session().update(b, off, len);
      } catch (SignatureException e) {
        throw e;
      } catch (GeneralSecurityException | RuntimeException e) {
        throw new SignatureException("Could not sign", e);
      }
    }

    @Override
    protected byte[] engineSign() throws SignatureException {
      ExternalKey.Session s = session();
      session = null;
      try {
        return s.sign();
      } catch (SignatureException e) {
        throw e;
      } catch (GeneralSecurityException e) {
        throw new SignatureException("Could not sign", e);
      } catch (RuntimeException e) {
        // A remote signer is caller code; its failures must not escape as unchecked exceptions.
        throw new SignatureException("The signer failed", e);
      }
    }

    @Override
    protected boolean engineVerify(byte[] sigBytes) throws SignatureException {
      throw new SignatureException("This provider only signs");
    }

    @Override
    @Deprecated
    @SuppressWarnings("deprecation")
    protected void engineSetParameter(String param, Object value) {
      throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    @SuppressWarnings("deprecation")
    protected @Nullable Object engineGetParameter(String param) {
      return null;
    }
  }
}
