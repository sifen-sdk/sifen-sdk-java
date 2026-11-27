/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.signing;

import io.github.sifensdk.core.SigningException;
import io.github.sifensdk.core.internal.keys.ExternalKey;
import io.github.sifensdk.core.internal.keys.ProviderBackend;
import io.github.sifensdk.core.internal.keys.RemoteBackend;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.InvalidParameterException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.ProviderException;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.security.auth.login.LoginException;
import org.jspecify.annotations.Nullable;

/**
 * The usual sources of {@link KeyMaterial}: PKCS#12 (path, bytes or stream), a {@link KeyStore}
 * with an alias, PEM, a PKCS#11 token or HSM, and a remote signer (ADR-0008, ADR-0012).
 *
 * <p>Passwords are only used while loading and are never kept; the caller owns the arrays and
 * should clear them afterwards. The returned key material is immutable, thread-safe, and its {@code
 * toString()} shows only the issuer's common name, the serial number and the end of validity of the
 * certificate, never its subject (the certificate of a natural person carries their name and
 * identity document). Errors while loading or adapting a key are {@link SigningException}s without
 * secrets; {@link #of(PrivateKey, List)} throws {@link IllegalArgumentException} for an empty
 * chain, a programming error.
 */
public final class KeyMaterials {

  private static final String PKCS12 = "PKCS12";

  private KeyMaterials() {}

  /**
   * Loads a PKCS#12 file ({@code .p12} or {@code .pfx}) with a single private key.
   *
   * @param path the file
   * @param password the password of the store and of the key
   * @return the key material
   * @throws SigningException if the file cannot be read, the password is wrong, or it does not have
   *     exactly one private key
   */
  public static KeyMaterial pkcs12(Path path, char[] password) {
    return pkcs12(path, password, null);
  }

  /**
   * Loads a PKCS#12 file ({@code .p12} or {@code .pfx}).
   *
   * @param path the file
   * @param password the password of the store and of the key
   * @param alias the alias of the private key; {@code null} if the store has exactly one
   * @return the key material
   * @throws SigningException if the file cannot be read, the password is wrong, or the key is not
   *     found
   */
  public static KeyMaterial pkcs12(Path path, char[] password, @Nullable String alias) {
    Objects.requireNonNull(path, "path");
    try (InputStream in = Files.newInputStream(path)) {
      return pkcs12(in, password, alias);
    } catch (IOException e) {
      throw new SigningException("Could not read the PKCS#12 file", e);
    }
  }

  /**
   * Loads a PKCS#12 store from its bytes, with a single private key.
   *
   * @param content the bytes of the store; not kept
   * @param password the password of the store and of the key
   * @return the key material
   * @throws SigningException if the store cannot be read, the password is wrong, or it does not
   *     have exactly one private key
   */
  public static KeyMaterial pkcs12(byte[] content, char[] password) {
    return pkcs12(content, password, null);
  }

  /**
   * Loads a PKCS#12 store from its bytes.
   *
   * @param content the bytes of the store; not kept
   * @param password the password of the store and of the key
   * @param alias the alias of the private key; {@code null} if the store has exactly one
   * @return the key material
   * @throws SigningException if the store cannot be read, the password is wrong, or the key is not
   *     found
   */
  public static KeyMaterial pkcs12(byte[] content, char[] password, @Nullable String alias) {
    Objects.requireNonNull(content, "content");
    return pkcs12(new ByteArrayInputStream(content), password, alias);
  }

  /**
   * Loads a PKCS#12 store from a stream, with a single private key. The stream is read but not
   * closed.
   *
   * @param in the stream with the store
   * @param password the password of the store and of the key
   * @return the key material
   * @throws SigningException if the store cannot be read, the password is wrong, or it does not
   *     have exactly one private key
   */
  public static KeyMaterial pkcs12(InputStream in, char[] password) {
    return pkcs12(in, password, null);
  }

  /**
   * Loads a PKCS#12 store from a stream. The stream is read but not closed.
   *
   * @param in the stream with the store
   * @param password the password of the store and of the key
   * @param alias the alias of the private key; {@code null} if the store has exactly one
   * @return the key material
   * @throws SigningException if the store cannot be read, the password is wrong, or the key is not
   *     found
   */
  public static KeyMaterial pkcs12(InputStream in, char[] password, @Nullable String alias) {
    Objects.requireNonNull(in, "in");
    Objects.requireNonNull(password, "password");
    KeyStore store;
    try {
      store = KeyStore.getInstance(PKCS12);
      store.load(in, password);
    } catch (IOException e) {
      if (e.getCause() instanceof UnrecoverableKeyException) {
        throw new SigningException(
            "Could not open the PKCS#12 store: the password is wrong or the store is damaged", e);
      }
      throw new SigningException("Could not read the PKCS#12 store", e);
    } catch (GeneralSecurityException e) {
      throw new SigningException("Could not read the PKCS#12 store", e);
    }
    return fromStore(store, resolveAlias(store, alias), password);
  }

  /**
   * Takes the key material from a loaded {@link KeyStore}: a PKCS#12 or JKS store, a PKCS#11 token
   * ({@code KeyStore.getInstance("PKCS11", provider)}) or the Windows store ({@code Windows-MY}).
   *
   * <p>A key that cannot leave its store (a token's) signs through the store's provider, even when
   * that provider is not installed. For a PKCS#11 token, {@link #pkcs11(Pkcs11Config, char[],
   * String)} is simpler.
   *
   * @param keyStore the loaded store
   * @param alias the alias of the private key
   * @param password the password of the key; {@code null} when the store does not use one (for
   *     example a PKCS#11 token already logged in)
   * @return the key material
   * @throws SigningException if the alias has no private key or certificate, or the password is
   *     wrong
   */
  public static KeyMaterial keyStore(KeyStore keyStore, String alias, char @Nullable [] password) {
    Objects.requireNonNull(keyStore, "keyStore");
    Objects.requireNonNull(alias, "alias");
    return fromStore(keyStore, alias, password);
  }

  /**
   * Takes the key material from PEM text: the certificate chain (one or more {@code -----BEGIN
   * CERTIFICATE-----} blocks, the signer's certificate first) and the private key in unencrypted
   * PKCS#8 ({@code -----BEGIN PRIVATE KEY-----}).
   *
   * <p>Encrypted keys ({@code BEGIN ENCRYPTED PRIVATE KEY}) and PKCS#1 keys ({@code BEGIN RSA
   * PRIVATE KEY}) are not supported: convert them first, for example with {@code openssl pkcs8
   * -topk8 -nocrypt}, or use PKCS#12.
   *
   * @param certificateChainPem the PEM certificates
   * @param privateKeyPem the PEM private key
   * @return the key material
   * @throws SigningException if the PEM cannot be read or the key does not belong to the first
   *     certificate
   */
  public static KeyMaterial pem(String certificateChainPem, String privateKeyPem) {
    Objects.requireNonNull(certificateChainPem, "certificateChainPem");
    Objects.requireNonNull(privateKeyPem, "privateKeyPem");
    List<X509Certificate> chain = readCertificates(certificateChainPem);
    PrivateKey key = readPrivateKey(privateKeyPem, chain.get(0));
    return of(key, chain);
  }

  /**
   * Reads the PEM certificate chain and the PEM private key from files. See {@link #pem(String,
   * String)}.
   *
   * @param certificateChainPem the file with the PEM certificates
   * @param privateKeyPem the file with the PEM private key
   * @return the key material
   * @throws SigningException if the files cannot be read or are not valid
   */
  public static KeyMaterial pem(Path certificateChainPem, Path privateKeyPem) {
    Objects.requireNonNull(certificateChainPem, "certificateChainPem");
    Objects.requireNonNull(privateKeyPem, "privateKeyPem");
    String certificates;
    String key;
    try {
      certificates = Files.readString(certificateChainPem, StandardCharsets.US_ASCII);
      key = Files.readString(privateKeyPem, StandardCharsets.US_ASCII);
    } catch (IOException e) {
      throw new SigningException("Could not read the PEM files", e);
    }
    return pem(certificates, key);
  }

  /**
   * Opens a PKCS#11 token or HSM (certificates on a token, type A3) and takes the key with a single
   * private key. See {@link #pkcs11(Pkcs11Config, char[], String)}.
   *
   * @param config the token's library and slot
   * @param pin the user PIN of the token
   * @return the key material
   * @throws SigningException if the library cannot be loaded, the PIN is wrong, or the token does
   *     not have exactly one private key with a certificate
   */
  public static KeyMaterial pkcs11(Pkcs11Config config, char[] pin) {
    return pkcs11(config, pin, null);
  }

  /**
   * Opens a PKCS#11 token or HSM (certificates on a token, type A3) and takes a key from it.
   *
   * <p>The key never leaves the device: every signature, of documents and events and of the TLS
   * handshake with SIFEN, is made by the token. The JDK's SunPKCS11 provider is used without
   * installing it (ADR-0012). The PIN is used only to log in and is not kept (clear the array
   * afterwards). PKCS#11 login belongs to the whole process: the token stays logged in while the
   * process runs, and opening it again in the same process does not check the PIN again. Each call
   * opens the token anew: create the key material once and reuse it.
   *
   * @param config the token's library and slot
   * @param pin the user PIN of the token
   * @param alias the alias (label) of the private key; {@code null} if the token has exactly one
   * @return the key material
   * @throws SigningException if the library cannot be loaded, the slot has no token, the PIN is
   *     wrong, or the key is not found; messages never include the PIN
   */
  public static KeyMaterial pkcs11(Pkcs11Config config, char[] pin, @Nullable String alias) {
    Objects.requireNonNull(config, "config");
    Objects.requireNonNull(pin, "pin");
    Path library = config.library();
    if (library != null && !Files.isRegularFile(library)) {
      throw new SigningException("The PKCS#11 library does not exist: " + library);
    }
    Path configFile = config.configFile();
    if (configFile != null && !Files.isRegularFile(configFile)) {
      throw new SigningException("The PKCS#11 configuration file does not exist: " + configFile);
    }
    Provider base = Security.getProvider("SunPKCS11");
    if (base == null) {
      throw new SigningException(
          "This Java runtime has no SunPKCS11 provider (module jdk.crypto.cryptoki)");
    }
    Provider token;
    try {
      token = base.configure(config.sunPkcs11Argument());
    } catch (InvalidParameterException | ProviderException | UnsupportedOperationException e) {
      throw new SigningException(
          "Could not load the PKCS#11 library or find the token's slot (" + config + ")", e);
    }
    KeyStore store;
    try {
      store = KeyStore.getInstance("PKCS11", token);
      store.load(null, pin);
    } catch (IOException e) {
      if (hasCause(e, LoginException.class)) {
        throw new SigningException(
            "Could not log in to the PKCS#11 token: the PIN is wrong or the token is locked", e);
      }
      throw new SigningException("Could not open the PKCS#11 token (" + config + ")", e);
    } catch (GeneralSecurityException | ProviderException e) {
      throw new SigningException("Could not open the PKCS#11 token (" + config + ")", e);
    }
    return fromStore(store, resolveAlias(store, alias), null);
  }

  /**
   * Adapts a remote signer (a signing service, a cloud KMS or HSM, or the certification provider's
   * custody): the key never reaches this process and the signer only receives SHA-256 digests,
   * never documents (ADR-0012).
   *
   * <p>The signer signs with RSASSA-PKCS1-v1_5, what SIFEN uses for XML signatures and in its TLS
   * 1.2 handshake; the TLS connection to SIFEN then uses TLS 1.2.
   *
   * @param signer the remote signer; its certificate chain is read now
   * @return the key material
   * @throws SigningException if the chain is empty or its first certificate does not have an RSA
   *     key
   */
  public static KeyMaterial remote(RemoteSigner signer) {
    Objects.requireNonNull(signer, "signer");
    List<X509Certificate> chain = List.copyOf(signer.certificateChain());
    if (chain.isEmpty()) {
      throw new SigningException("The remote signer has no certificate");
    }
    if (!(chain.get(0).getPublicKey() instanceof RSAPublicKey)) {
      throw new SigningException(
          "SIFEN signatures use RSA (Manual Técnico v150, 7.7); the remote signer's key is "
              + chain.get(0).getPublicKey().getAlgorithm());
    }
    java.math.BigInteger modulus = ((RSAPublicKey) chain.get(0).getPublicKey()).getModulus();
    return of(
        new ExternalKey(
            new RemoteBackend(signer, chain.get(0).getPublicKey(), modulus.bitLength()), modulus),
        chain);
  }

  /**
   * Wraps a private key and its certificate chain.
   *
   * @param privateKey the private key
   * @param certificateChain the chain, starting with the certificate of the key; copied
   * @return the key material
   * @throws IllegalArgumentException if the chain is empty
   */
  public static KeyMaterial of(PrivateKey privateKey, List<X509Certificate> certificateChain) {
    Objects.requireNonNull(privateKey, "privateKey");
    List<X509Certificate> chain = List.copyOf(certificateChain);
    if (chain.isEmpty()) {
      throw new IllegalArgumentException("The certificate chain is empty");
    }
    return new LoadedKeyMaterial(privateKey, chain);
  }

  private static String resolveAlias(KeyStore store, @Nullable String alias) {
    try {
      if (alias != null) {
        if (!store.isKeyEntry(alias)) {
          throw new SigningException("The store has no private key with the given alias");
        }
        return alias;
      }
      List<String> keyAliases = new ArrayList<>();
      for (String candidate : Collections.list(store.aliases())) {
        if (store.isKeyEntry(candidate)) {
          keyAliases.add(candidate);
        }
      }
      if (keyAliases.isEmpty()) {
        throw new SigningException("The store has no private key");
      }
      if (keyAliases.size() > 1) {
        Collections.sort(keyAliases);
        throw new SigningException(
            "The store has "
                + keyAliases.size()
                + " private keys "
                + keyAliases
                + "; choose one by its alias");
      }
      return keyAliases.get(0);
    } catch (KeyStoreException e) {
      throw new SigningException("Could not read the key store", e);
    }
  }

  private static KeyMaterial fromStore(KeyStore store, String alias, char @Nullable [] password) {
    try {
      Key key = store.getKey(alias, password);
      if (!(key instanceof PrivateKey)) {
        throw new SigningException("The store has no private key with the given alias");
      }
      Certificate[] certificates = store.getCertificateChain(alias);
      if (certificates == null || certificates.length == 0) {
        throw new SigningException("The private key in the store has no certificate");
      }
      List<X509Certificate> chain = new ArrayList<>(certificates.length);
      for (Certificate certificate : certificates) {
        if (!(certificate instanceof X509Certificate)) {
          throw new SigningException("The certificate chain is not X.509");
        }
        chain.add((X509Certificate) certificate);
      }
      return of(signable((PrivateKey) key, chain.get(0), store), chain);
    } catch (UnrecoverableKeyException e) {
      throw new SigningException("Could not recover the private key: the password is wrong", e);
    } catch (GeneralSecurityException e) {
      throw new SigningException("Could not read the private key from the store", e);
    }
  }

  /**
   * A key of a store whose provider is not installed (a SunPKCS11 instance configured by the
   * caller) signs through that provider (ADR-0012). Keys of installed providers, and keys that can
   * be exported, are returned as they are.
   */
  private static PrivateKey signable(PrivateKey key, X509Certificate certificate, KeyStore store) {
    Provider provider = store.getProvider();
    if (Security.getProvider(provider.getName()) == provider
        || !"RSA".equals(key.getAlgorithm())
        || key.getEncoded() != null
        || !(certificate.getPublicKey() instanceof RSAPublicKey)) {
      return key;
    }
    return new ExternalKey(
        new ProviderBackend(provider, key, "key store " + store.getType()),
        ((RSAPublicKey) certificate.getPublicKey()).getModulus());
  }

  private static boolean hasCause(Throwable e, Class<? extends Throwable> type) {
    for (Throwable t = e; t != null; t = t.getCause()) {
      if (type.isInstance(t)) {
        return true;
      }
    }
    return false;
  }

  private static List<X509Certificate> readCertificates(String pem) {
    if (!pem.contains("-----BEGIN CERTIFICATE-----")) {
      throw new SigningException("The PEM text has no certificate (BEGIN CERTIFICATE)");
    }
    Collection<? extends Certificate> certificates;
    try {
      certificates =
          CertificateFactory.getInstance("X.509")
              .generateCertificates(
                  new ByteArrayInputStream(pem.getBytes(StandardCharsets.US_ASCII)));
    } catch (CertificateException e) {
      throw new SigningException("Could not read the PEM certificates", e);
    }
    List<X509Certificate> chain = new ArrayList<>(certificates.size());
    for (Certificate certificate : certificates) {
      chain.add((X509Certificate) certificate);
    }
    if (chain.isEmpty()) {
      throw new SigningException("The PEM text has no certificate (BEGIN CERTIFICATE)");
    }
    return chain;
  }

  private static PrivateKey readPrivateKey(String pem, X509Certificate certificate) {
    if (pem.contains("-----BEGIN ENCRYPTED PRIVATE KEY-----")) {
      throw new SigningException(
          "Encrypted PEM private keys are not supported: decrypt it to unencrypted PKCS#8"
              + " (BEGIN PRIVATE KEY) or use PKCS#12");
    }
    if (pem.contains("-----BEGIN RSA PRIVATE KEY-----")
        || pem.contains("-----BEGIN EC PRIVATE KEY-----")) {
      throw new SigningException(
          "PKCS#1/SEC1 PEM private keys are not supported: convert it to unencrypted PKCS#8"
              + " (BEGIN PRIVATE KEY), for example with openssl pkcs8 -topk8 -nocrypt");
    }
    String begin = "-----BEGIN PRIVATE KEY-----";
    String end = "-----END PRIVATE KEY-----";
    int start = pem.indexOf(begin);
    int stop = pem.indexOf(end);
    if (start < 0 || stop < start) {
      throw new SigningException("The PEM text has no private key (BEGIN PRIVATE KEY)");
    }
    byte[] der;
    try {
      der = Base64.getMimeDecoder().decode(pem.substring(start + begin.length(), stop));
    } catch (IllegalArgumentException e) {
      throw new SigningException("The PEM private key is not valid Base64");
    }
    try {
      String algorithm = certificate.getPublicKey().getAlgorithm();
      PrivateKey key =
          KeyFactory.getInstance(algorithm).generatePrivate(new PKCS8EncodedKeySpec(der));
      if (key instanceof RSAPrivateCrtKey
          && certificate.getPublicKey() instanceof RSAPublicKey
          && !((RSAPrivateCrtKey) key)
              .getModulus()
              .equals(((RSAPublicKey) certificate.getPublicKey()).getModulus())) {
        throw new SigningException("The PEM private key does not belong to the first certificate");
      }
      return key;
    } catch (GeneralSecurityException e) {
      throw new SigningException(
          "Could not read the PEM private key, or it does not match the certificate's algorithm",
          e);
    } finally {
      Arrays.fill(der, (byte) 0);
    }
  }

  /** Key material held in memory (or as a handle to a token key). */
  private static final class LoadedKeyMaterial implements KeyMaterial {

    private final PrivateKey privateKey;
    private final List<X509Certificate> chain;

    LoadedKeyMaterial(PrivateKey privateKey, List<X509Certificate> chain) {
      this.privateKey = privateKey;
      this.chain = chain;
    }

    @Override
    public PrivateKey privateKey() {
      return privateKey;
    }

    @Override
    public X509Certificate certificate() {
      return chain.get(0);
    }

    @Override
    public List<X509Certificate> certificateChain() {
      return chain;
    }

    /** Returns the issuer's common name, the serial number and the end of validity. */
    @Override
    public String toString() {
      X509Certificate c = certificate();
      return "KeyMaterial[issuer="
          + commonName(c.getIssuerX500Principal().getName())
          + ", serialNumber="
          + c.getSerialNumber().toString(16)
          + ", notAfter="
          + c.getNotAfter().toInstant()
          + "]";
    }

    /** The value of the first CN of an RFC 2253 name, unescaped, or "?" if it has none. */
    private static String commonName(String rfc2253) {
      int i = 0;
      while (i < rfc2253.length()) {
        int end = i;
        boolean escaped = false;
        while (end < rfc2253.length()) {
          char ch = rfc2253.charAt(end);
          if (escaped) {
            escaped = false;
          } else if (ch == '\\') {
            escaped = true;
          } else if (ch == ',' || ch == '+') {
            break;
          }
          end++;
        }
        String rdn = rfc2253.substring(i, end);
        if (rdn.regionMatches(true, 0, "CN=", 0, 3)) {
          return rdn.substring(3).replaceAll("\\\\(.)", "$1");
        }
        i = end + 1;
      }
      return "?";
    }
  }
}
