/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.internal.keys;

import java.io.NotSerializableException;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.interfaces.RSAKey;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * An RSA private key that the SDK cannot read: a handle that signs through its {@link Backend} (a
 * token's provider or a remote signer). Only {@link SifenKeysProvider} signs with it.
 *
 * <p>It implements {@link RSAKey} (the modulus, from the certificate) because JSSE needs the key
 * size to choose a TLS signature scheme, and not {@link java.security.interfaces.RSAPrivateKey}, so
 * that no other provider tries to use it. It holds no key bytes: {@link #getEncoded()} is {@code
 * null}. Immutable and thread-safe as long as its backend is. Not serializable.
 */
public final class ExternalKey implements PrivateKey, RSAKey {

  private static final long serialVersionUID = 1L;

  private final transient Backend backend;

  /** The RSA modulus of the certificate's public key. */
  private final BigInteger modulus;

  /**
   * Creates the handle.
   *
   * @param backend what signs
   * @param modulus the RSA modulus of the certificate's public key
   */
  public ExternalKey(Backend backend, BigInteger modulus) {
    this.backend = Objects.requireNonNull(backend, "backend");
    this.modulus = Objects.requireNonNull(modulus, "modulus");
  }

  /**
   * Whether the backend can sign with a JCA signature algorithm.
   *
   * @param algorithm {@code SHA256withRSA} or {@code RSASSA-PSS}
   * @return whether it is supported
   */
  public boolean supports(String algorithm) {
    return backend.supports(algorithm);
  }

  Backend backend() {
    return backend;
  }

  @Override
  public String getAlgorithm() {
    return "RSA";
  }

  @Override
  public @Nullable String getFormat() {
    return null;
  }

  @Override
  public byte @Nullable [] getEncoded() {
    return null;
  }

  @Override
  public BigInteger getModulus() {
    return modulus;
  }

  @Override
  public String toString() {
    return "ExternalKey[RSA " + modulus.bitLength() + " bits, " + backend.description() + "]";
  }

  /**
   * Refuses to serialize: the key only works in the process that holds its token or signer.
   *
   * @param out not used
   * @throws NotSerializableException always
   */
  private void writeObject(ObjectOutputStream out) throws NotSerializableException {
    throw new NotSerializableException("A key held in a token or by a remote signer");
  }

  /** What signs with an {@link ExternalKey}. Implementations must be thread-safe. */
  public interface Backend {

    /**
     * Whether it can sign with a JCA signature algorithm.
     *
     * @param algorithm {@code SHA256withRSA} or {@code RSASSA-PSS}
     * @return whether it is supported
     */
    boolean supports(String algorithm);

    /**
     * Starts one signature.
     *
     * @param algorithm a supported algorithm
     * @param parameters the algorithm parameters ({@code RSASSA-PSS}), or {@code null}
     * @return a session that receives the data and returns the signature
     * @throws GeneralSecurityException if the signature cannot be started
     */
    Session open(String algorithm, @Nullable AlgorithmParameterSpec parameters)
        throws GeneralSecurityException;

    /**
     * Describes the backend without secrets, for {@code toString()}.
     *
     * @return for example {@code "PKCS#11 token"}
     */
    String description();
  }

  /**
   * One signature in progress. Not thread-safe; used by a single {@link java.security.Signature}.
   */
  public interface Session {

    /**
     * Adds data to sign.
     *
     * @param data the array
     * @param offset where the data starts
     * @param length how many bytes
     * @throws GeneralSecurityException if the backend fails
     */
    void update(byte[] data, int offset, int length) throws GeneralSecurityException;

    /**
     * Returns the signature of all the data added.
     *
     * @return the signature
     * @throws GeneralSecurityException if the backend fails
     */
    byte[] sign() throws GeneralSecurityException;
  }
}
