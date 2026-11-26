/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.signing;

import io.github.sifensdk.core.SigningException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
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
import org.jspecify.annotations.Nullable;

/**
 * The usual sources of {@link KeyMaterial}: PKCS#12 (path, bytes or stream), a {@link KeyStore}
 * with an alias (which also covers PKCS#11 tokens) and PEM (ADR-0008).
 *
 * <p>Passwords are only used while loading and are never kept; the caller owns the arrays and
 * should clear them afterwards. The returned key material is immutable, thread-safe, and its {@code
 * toString()} shows only the subject of the certificate. Errors are {@link SigningException}s
 * without secrets.
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
   * Takes the key material from a loaded {@link KeyStore}: a PKCS#12 or JKS store, or a PKCS#11
   * token ({@code KeyStore.getInstance("PKCS11", provider)}), whose key never leaves the device.
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
      return of((PrivateKey) key, chain);
    } catch (UnrecoverableKeyException e) {
      throw new SigningException("Could not recover the private key: the password is wrong", e);
    } catch (GeneralSecurityException e) {
      throw new SigningException("Could not read the private key from the store", e);
    }
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

    @Override
    public String toString() {
      return "KeyMaterial[subject=" + certificate().getSubjectX500Principal() + "]";
    }
  }
}
