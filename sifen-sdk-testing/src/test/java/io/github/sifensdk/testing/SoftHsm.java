/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.testing;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import io.github.sifensdk.core.signing.KeyMaterial;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.Provider;
import java.security.Security;
import java.security.cert.Certificate;
import java.util.List;
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
final class SoftHsm {

  static final char[] PIN = "1234".toCharArray();
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
  static Path library() {
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

  /** A token with the synthetic client key under the alias {@code firma}. */
  static long singleKeyToken() {
    requireAvailable();
    return Tokens.INSTANCE.single;
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

    private Tokens() {
      Path library = library();
      String conf = System.getenv("SOFTHSM2_CONF");
      assumeTrue(conf != null, "SOFTHSM2_CONF is not set");
      Path confPath = Path.of(conf);
      KeyMaterial firma = SyntheticClient.keyMaterial();
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
        importKeys(library, singleSlot, firma);
        this.single = singleSlot;
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
              .configure("--name = Prueba\nlibrary = \"" + library + "\"\nslot = " + slot + "\n");
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
