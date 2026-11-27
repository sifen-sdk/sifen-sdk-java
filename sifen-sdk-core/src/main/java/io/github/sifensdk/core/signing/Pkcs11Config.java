/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.signing;

import java.nio.file.Path;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Where a PKCS#11 token or HSM is: the manufacturer's library and the slot. Used by {@link
 * KeyMaterials#pkcs11(Pkcs11Config, char[], String)}.
 *
 * <p>The SDK uses the JDK's SunPKCS11 provider without installing it (ADR-0012). Either give the
 * library and, if the library has several slots, the slot; or point to a SunPKCS11 configuration
 * file for advanced settings. Immutable and thread-safe.
 */
public final class Pkcs11Config {

  private final @Nullable Path library;
  private final @Nullable Long slot;
  private final @Nullable Integer slotIndex;
  private final @Nullable Path configFile;

  private Pkcs11Config(
      @Nullable Path library,
      @Nullable Long slot,
      @Nullable Integer slotIndex,
      @Nullable Path configFile) {
    this.library = library;
    this.slot = slot;
    this.slotIndex = slotIndex;
    this.configFile = configFile;
  }

  /**
   * The token in the first slot of a library, for example {@code /usr/lib/libeToken.so} or {@code
   * C:\Windows\System32\eTPKCS11.dll}.
   *
   * @param library the PKCS#11 library of the token or HSM
   * @return the configuration
   */
  public static Pkcs11Config library(Path library) {
    return new Pkcs11Config(Objects.requireNonNull(library, "library"), null, null, null);
  }

  /**
   * The token in a slot, by its identifier ({@code slotID}, as listed by {@code pkcs11-tool -L}).
   *
   * @param library the PKCS#11 library of the token or HSM
   * @param slot the slot identifier
   * @return the configuration
   */
  public static Pkcs11Config slot(Path library, long slot) {
    return new Pkcs11Config(Objects.requireNonNull(library, "library"), slot, null, null);
  }

  /**
   * The token in a slot, by its position in the library's list of slots, starting at 0.
   *
   * @param library the PKCS#11 library of the token or HSM
   * @param index the position of the slot
   * @return the configuration
   * @throws IllegalArgumentException if the index is negative
   */
  public static Pkcs11Config slotIndex(Path library, int index) {
    if (index < 0) {
      throw new IllegalArgumentException("The slot index starts at 0");
    }
    return new Pkcs11Config(Objects.requireNonNull(library, "library"), null, index, null);
  }

  /**
   * A SunPKCS11 configuration file, for settings this class does not cover. See the JDK's PKCS#11
   * reference guide for its format.
   *
   * @param configFile the configuration file
   * @return the configuration
   */
  public static Pkcs11Config configFile(Path configFile) {
    return new Pkcs11Config(null, null, null, Objects.requireNonNull(configFile, "configFile"));
  }

  /**
   * Returns the library, or {@code null} for a configuration file.
   *
   * @return the library
   */
  public @Nullable Path library() {
    return library;
  }

  /**
   * Returns the configuration file, or {@code null} when the library is given.
   *
   * @return the configuration file
   */
  public @Nullable Path configFile() {
    return configFile;
  }

  /** The argument of {@code Provider.configure}: inline text ({@code --...}) or a file path. */
  String sunPkcs11Argument() {
    if (configFile != null) {
      return configFile.toString();
    }
    StringBuilder sb = new StringBuilder("--name = SifenSdk\n");
    // Quoted, because paths may have spaces; inside quotes backslashes and quotes are escaped.
    String path =
        Objects.requireNonNull(library)
            .toAbsolutePath()
            .toString()
            .replace("\\", "\\\\")
            .replace("\"", "\\\"");
    sb.append("library = \"").append(path).append("\"\n");
    if (slot != null) {
      sb.append("slot = ").append(slot).append('\n');
    } else if (slotIndex != null) {
      sb.append("slotListIndex = ").append(slotIndex).append('\n');
    }
    return sb.toString();
  }

  /** Describes it without secrets (it has none). */
  @Override
  public String toString() {
    if (configFile != null) {
      return "Pkcs11Config[configFile=" + configFile + "]";
    }
    return "Pkcs11Config[library="
        + library
        + (slot != null ? ", slot=" + slot : "")
        + (slotIndex != null ? ", slotIndex=" + slotIndex : "")
        + "]";
  }
}
