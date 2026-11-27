/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.signing;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.Provider;
import java.security.Security;
import java.security.cert.Certificate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A SoftHSM2 token (a PKCS#11 token in software) with a copy of the synthetic test key, for the
 * token tests. Skips the test when SoftHSM2 is not installed (fails instead with {@code
 * SIFEN_REQUIRE_SOFTHSM=true}, as in CI on Linux) or {@code SOFTHSM2_CONF} is not set (the build
 * sets it for the tests; see the parent {@code pom.xml}).
 */
public final class SoftHsm {

  public static final char[] PIN = "1234".toCharArray();
  private static final String SO_PIN = "5678";
  private static final List<String> LIBRARIES =
      List.of(
          "/usr/lib/softhsm/libsofthsm2.so",
          "/usr/lib/x86_64-linux-gnu/softhsm/libsofthsm2.so",
          "/usr/lib/aarch64-linux-gnu/softhsm/libsofthsm2.so",
          "/usr/local/lib/softhsm/libsofthsm2.so",
          "/opt/homebrew/lib/softhsm/libsofthsm2.so");

  private SoftHsm() {}

  /** The SoftHSM2 library, or skips the test. */
  public static Path library() {
    String override = System.getenv("SOFTHSM2_LIB");
    if (override != null && Files.isRegularFile(Path.of(override))) {
      return Path.of(override);
    }
    for (String candidate : LIBRARIES) {
      if (Files.isRegularFile(Path.of(candidate))) {
        return Path.of(candidate);
      }
    }
    if ("true".equals(System.getenv("SIFEN_REQUIRE_SOFTHSM"))) {
      throw new IllegalStateException(
          "SoftHSM2 is required (SIFEN_REQUIRE_SOFTHSM) but not installed");
    }
    assumeTrue(false, "SoftHSM2 is not installed");
    throw new AssertionError();
  }

  /** A token with the synthetic key ({@code prueba.p12}) under the alias {@code firma}. */
  public static long singleKeyToken() {
    requireAvailable();
    return Tokens.INSTANCE.single;
  }

  /** A token with the synthetic key ({@code firma}) and a second one ({@code otra}). */
  public static long twoKeyToken() {
    requireAvailable();
    return Tokens.INSTANCE.two;
  }

  /** A token without keys that nothing in this process has logged in to. */
  public static long emptyToken() {
    requireAvailable();
    return Tokens.INSTANCE.empty;
  }

  /**
   * Checks outside the token holder's initializer: an assumption that fails inside a static
   * initializer is an error, not a skipped test.
   */
  private static void requireAvailable() {
    library();
    assumeTrue(System.getenv("SOFTHSM2_CONF") != null, "SOFTHSM2_CONF is not set");
    try {
      run("softhsm2-util", "--version");
    } catch (IOException | IllegalStateException e) {
      assumeTrue(false, "softhsm2-util is not available");
    }
  }

  /**
   * SoftHSM2 reads its tokens when the library is loaded, once per process, so both tokens are
   * created together before the first use of the library.
   */
  private static final class Tokens {

    static final Tokens INSTANCE = new Tokens();
    final long single;
    final long two;
    final long empty;

    private Tokens() {
      Path library = library();
      String conf = System.getenv("SOFTHSM2_CONF");
      assumeTrue(conf != null, "SOFTHSM2_CONF is not set");
      Path confPath = Path.of(conf);
      char[] password = "prueba-sintetica".toCharArray();
      KeyMaterial firma = KeyMaterials.pkcs12(signing("prueba.p12"), password);
      KeyMaterial otra = KeyMaterials.pkcs12(signing("dos-alias.p12"), password, "otra");
      try {
        Path tokens = confPath.resolveSibling("tokens");
        Files.createDirectories(tokens);
        Files.writeString(
            confPath,
            "directories.tokendir = "
                + tokens.toAbsolutePath()
                + "\nobjectstore.backend = file\nlog.level = ERROR\n",
            StandardCharsets.UTF_8);
        long singleSlot = initToken();
        long twoSlot = initToken();
        this.empty = initToken();
        importKeys(library, singleSlot, firma);
        importKeys(library, twoSlot, firma, otra);
        this.single = singleSlot;
        this.two = twoSlot;
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      } catch (GeneralSecurityException e) {
        throw new IllegalStateException(e);
      }
    }

    private static long initToken() throws IOException {
      String label = "sdk-" + UUID.randomUUID().toString().substring(0, 8);
      String out =
          run(
              "softhsm2-util",
              "--init-token",
              "--free",
              "--label",
              label,
              "--pin",
              new String(PIN),
              "--so-pin",
              SO_PIN);
      Matcher m = Pattern.compile("reassigned to slot (\\d+)").matcher(out);
      if (!m.find()) {
        throw new IllegalStateException("softhsm2-util did not say the slot: " + out);
      }
      return Long.parseLong(m.group(1));
    }

    private static void importKeys(Path library, long slot, KeyMaterial... keys)
        throws IOException, GeneralSecurityException {
      Provider provider =
          Security.getProvider("SunPKCS11")
              .configure(Pkcs11Config.slot(library, slot).sunPkcs11Argument());
      KeyStore token = KeyStore.getInstance("PKCS11", provider);
      token.load(null, PIN);
      String[] aliases = {"firma", "otra"};
      for (int i = 0; i < keys.length; i++) {
        token.setKeyEntry(
            aliases[i],
            keys[i].privateKey(),
            null,
            keys[i].certificateChain().toArray(new Certificate[0]));
      }
    }

    private static Path signing(String name) {
      try {
        return Path.of(
            Objects.requireNonNull(SoftHsm.class.getResource("/signing/" + name), name).toURI());
      } catch (URISyntaxException e) {
        throw new IllegalStateException(e);
      }
    }
  }

  private static String run(String... command) throws IOException {
    ProcessBuilder builder = new ProcessBuilder(command).redirectErrorStream(true);
    Process process;
    try {
      process = builder.start();
    } catch (IOException e) {
      assumeTrue(false, "softhsm2-util is not installed");
      throw e;
    }
    try {
      String out = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
      if (!process.waitFor(30, TimeUnit.SECONDS) || process.exitValue() != 0) {
        throw new IllegalStateException("softhsm2-util failed: " + out);
      }
      return out;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException(e);
    }
  }
}
