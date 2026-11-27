/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.signing;

import io.github.sifensdk.core.validation.Severity;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.security.auth.x500.X500Principal;
import org.jspecify.annotations.Nullable;

/**
 * Checks of the signer's certificate before using it (ADR-0008, ADR-0012).
 *
 * <p>{@code SifenDocuments.sign} and {@code SifenEvents.sign} refuse to sign when there is an
 * error: a certificate out of its validity period, or one whose key usage allows neither digital
 * signature nor non-repudiation. A RUC in the certificate that is not the issuer's is only a
 * warning: SIFEN accepts documents signed with the certificate of a natural person authorized by
 * the issuer, whose certificate carries the person's identity and not the issuer's RUC.
 */
public final class CertificateChecks {

  /** The certificate's validity has not started yet. Error. */
  public static final String NOT_YET_VALID = "certificate.not-yet-valid";

  /** The certificate's validity has ended. Error. */
  public static final String EXPIRED = "certificate.expired";

  /** The key usage allows neither digital signature nor non-repudiation. Error. */
  public static final String NO_SIGNING_USAGE = "certificate.no-signing-usage";

  /** The certificate names a RUC that is not the issuer's. Warning. */
  public static final String RUC_MISMATCH = "certificate.ruc-mismatch";

  private static final Map<String, String> SERIAL_NUMBER_KEYWORD =
      Map.of("2.5.4.5", "SERIALNUMBER");
  private static final Pattern SERIAL_NUMBER = Pattern.compile("(?:^|,)SERIALNUMBER=([^,+]*)");

  /** With its check digit after a dash; without the dash the number and the digit are ambiguous. */
  private static final Pattern RUC = Pattern.compile("RUC\\s*([0-9]{1,8})-[0-9]");

  private CertificateChecks() {}

  /**
   * Checks the signer's certificate.
   *
   * @param keyMaterial the key material whose certificate is checked
   * @param at the moment of the signature
   * @param issuerRuc the issuer's RUC, with or without its check digit ({@code 80000000} or {@code
   *     80000000-1}), to compare with the RUC in the certificate; {@code null} to skip it
   * @return the issues found, errors first; empty if there are none
   */
  public static List<Issue> check(KeyMaterial keyMaterial, Instant at, @Nullable String issuerRuc) {
    Objects.requireNonNull(keyMaterial, "keyMaterial");
    Objects.requireNonNull(at, "at");
    X509Certificate certificate = keyMaterial.certificate();
    List<Issue> issues = new ArrayList<>();
    Date when = Date.from(at);
    if (when.before(certificate.getNotBefore())) {
      issues.add(
          new Issue(
              Severity.ERROR,
              NOT_YET_VALID,
              "The signer's certificate is valid from "
                  + certificate.getNotBefore().toInstant()
                  + "; it cannot sign before"));
    } else if (when.after(certificate.getNotAfter())) {
      issues.add(
          new Issue(
              Severity.ERROR,
              EXPIRED,
              "The signer's certificate expired on " + certificate.getNotAfter().toInstant()));
    }
    boolean[] usage = certificate.getKeyUsage();
    if (usage != null && !(bit(usage, 0) || bit(usage, 1))) {
      issues.add(
          new Issue(
              Severity.ERROR,
              NO_SIGNING_USAGE,
              "The signer's certificate does not allow digital signature (key usage)"));
    }
    if (issuerRuc != null) {
      String expected = rucNumber(issuerRuc);
      String inCertificate = rucIn(certificate);
      if (inCertificate != null && !inCertificate.equals(expected)) {
        issues.add(
            new Issue(
                Severity.WARNING,
                RUC_MISMATCH,
                "The signer's certificate names another RUC than the issuer's; SIFEN accepts it"
                    + " only if its holder is authorized to sign for the issuer"));
      }
    }
    return List.copyOf(issues);
  }

  private static boolean bit(boolean[] usage, int index) {
    return usage.length > index && usage[index];
  }

  private static String rucNumber(String ruc) {
    String trimmed = ruc.replaceAll("[\\s.]", "");
    int dash = trimmed.indexOf('-');
    String number = dash < 0 ? trimmed : trimmed.substring(0, dash);
    return number.replaceFirst("^0+(?=.)", "");
  }

  /** The RUC in the subject's serial number ({@code RUC80000000-1}), as Paraguayan CAs write it. */
  private static @Nullable String rucIn(X509Certificate certificate) {
    String subject =
        certificate
            .getSubjectX500Principal()
            .getName(X500Principal.RFC2253, SERIAL_NUMBER_KEYWORD)
            .toUpperCase(Locale.ROOT);
    Matcher serialNumber = SERIAL_NUMBER.matcher(subject);
    while (serialNumber.find()) {
      Matcher m = RUC.matcher(serialNumber.group(1));
      if (m.find()) {
        return m.group(1).replaceFirst("^0+(?=.)", "");
      }
    }
    return null;
  }

  /**
   * An issue of the signer's certificate.
   *
   * @param severity {@link Severity#ERROR} if it prevents signing, {@link Severity#WARNING}
   *     otherwise
   * @param code a stable code, one of the constants of {@link CertificateChecks}
   * @param message a description in English, without the certificate's personal data
   */
  public record Issue(Severity severity, String code, String message) {

    /**
     * Creates the issue.
     *
     * @param severity the severity
     * @param code the code
     * @param message the description
     */
    public Issue {
      Objects.requireNonNull(severity, "severity");
      Objects.requireNonNull(code, "code");
      Objects.requireNonNull(message, "message");
    }

    /**
     * Returns whether the issue prevents signing.
     *
     * @return true if its severity is {@link Severity#ERROR}
     */
    public boolean isError() {
      return severity == Severity.ERROR;
    }
  }
}
