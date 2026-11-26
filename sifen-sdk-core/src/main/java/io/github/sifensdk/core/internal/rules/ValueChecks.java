/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.internal.rules;

import io.github.sifensdk.core.Environment;
import io.github.sifensdk.core.internal.spec.CatalogEntry;
import io.github.sifensdk.core.internal.spec.Field;
import io.github.sifensdk.core.internal.spec.Rule;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;

/**
 * Java implementations of {@code formato} and {@code dependencia} rules that can be verified with
 * the document alone, registered by rule id.
 *
 * <p>Each entry cites the rule; the rule (and its sources) is in {@code spec/reglas}. Rules that
 * need SIFEN's own data (taxpayer registry, stamps, duplicates, SIFEN's clock) are not here and are
 * reported as unchecked. When the source admits two readings, the check implements the more lenient
 * one, so the SDK never rejects what SIFEN may accept (noted next to each such entry).
 *
 * <p>Besides the entries listed in {@link #all()}, every {@code dependencia} rule that requires a
 * description to match its code (for example {@code R-C003-D1}) is registered by {@link Engine}
 * with {@link #description(String, String)}, from the catalogs' {@code descripciones}.
 */
final class ValueChecks {

  private static final Pattern DATE_TIME =
      Pattern.compile("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}");
  private static final Pattern DATE = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
  private static final Pattern DIGITS = Pattern.compile("\\d+");

  /** MT150 D105 / 1263: literal required in the issuer name in the test environment. */
  static final String TEST_ISSUER_NAME =
      "DE generado en ambiente de prueba - sin valor comercial ni fiscal";

  /** MT150 1156: earliest issue date accepted by SIFEN (exclusive in the source, see below). */
  static final LocalDate EARLIEST_ISSUE_DATE = LocalDate.of(2018, 11, 22);

  private ValueChecks() {}

  static Map<String, Check> all() {
    Map<String, Check> m = new HashMap<>();

    // Structure and identification.
    m.put("R-AA002-F1", textIs("150", "The format version must be 150"));
    m.put("R-A002-D1", ValueChecks::cdcComposition);
    m.put("R-A003-F1", ValueChecks::cdcCheckDigit);
    m.put("R-A004-F1", dateTime());
    m.put("R-D002-F1", dateTime());
    m.put("R-B002-F1", textIsNot("2", "Contingency issuing (B002 = 2) is not allowed"));
    m.put("R-B004-F1", ValueChecks::securityCode);

    // Stamp and numbering: "completar con 0 a la izquierda" means exactly the field length.
    m.put("R-C005-F1", fixedLength());
    m.put("R-C006-F1", fixedLength());
    m.put("R-C007-F1", fixedLength());
    m.put("R-H006-F1", fixedLength());
    m.put("R-H007-F1", fixedLength());
    m.put("R-H008-F1", fixedLength());
    m.put("R-E631-F1", fixedLength());
    m.put("R-E704-F1", fixedLength());
    // NT001 1103: D002 "posterior" to C008. Same-day issuing is ambiguous: only earlier is
    // rejected.
    m.put(
        "R-C004-F4",
        dateNotBefore(
            "D002",
            "C008",
            "The issue date (D002) is before the stamp" + " validity start (C008)"));

    // Dates.
    // MT150 1156: "posterior al 22 de noviembre del 2018"; only dates before it are rejected.
    m.put("R-D002-F4", ValueChecks::issueDateAfterStart);
    // C-203: validation 1402 ("no puede ser superior").
    m.put("R-E025-D1", dateNotAfter("E025", "D002", "Must not be after the issue date (D002)"));
    m.put("R-E824-D1", dateNotBefore("E824", "D002", "Must not be before the issue date (D002)"));
    // C-401: compared with the cycle start date E822.
    m.put("R-E823-D1", dateNotBefore("E823", "E822", "Must not be before the cycle start (E822)"));
    // C-402: compared with the estimated transfer start E909.
    m.put(
        "R-E910-D1", dateNotBefore("E910", "E909", "Must not be before the transfer start (E909)"));
    m.put("R-E752-F1", date());
    m.put("R-H011-F1", date());

    // Issuer.
    m.put("R-D102-F1", checkDigitOf("D101"));
    m.put("R-D105-F1", ValueChecks::testIssuerName);
    m.put("R-D111-F1", location("D111", "D113", "D115", LocationCheck.ALL));
    m.put("R-D115-F1", location("D111", "D113", "D115", LocationCheck.CITY_DEPARTMENT));
    m.put("R-D115-F2", location("D111", "D113", "D115", LocationCheck.CITY_DISTRICT));

    // Receiver.
    m.put("R-D201-F1", textIs("1", "The receiver must be a taxpayer (D201 = 1)"));
    m.put("R-D202-F1", textIs("2", "The operation type must be B2C (D202 = 2)"));
    m.put("R-D202-F2", otherIs("D201", "2", "The receiver must be a non-taxpayer (D201 = 2)"));
    m.put("R-D202-F3", textIn(Set.of("2", "4"), "The operation type must be B2C or B2F"));
    m.put("R-D202-F4", textIs("2", "The operation type must be B2C (D202 = 2)"));
    m.put("R-D203-F1", textIsNot("PRY", "The country must not be PRY for B2F operations"));
    m.put("R-D203-F2", textIs("PRY", "The country must be PRY unless the operation is B2F"));
    m.put("R-D206-D1", sameAs("D101", "Must equal the issuer RUC (D101)"));
    m.put("R-D207-F1", checkDigitOf("D206"));
    for (String id :
        List.of(
            "R-D208-F1",
            "R-D208-F2",
            "R-D208-F3",
            "R-D208-F4",
            "R-D208-F5",
            "R-D208-F6",
            "R-D208-F7")) {
      m.put(id, textIsNot("5", "The receiver identity document must not be unnamed (D208 = 5)"));
    }
    m.put("R-D210-D1", textIs("0", "Must be 0 for an unnamed receiver (D208 = 5)"));
    m.put("R-D211-D1", textIs("Sin Nombre", "Must be the literal for an unnamed receiver"));
    m.put("R-D223-F1", location("D219", "D221", "D223", LocationCheck.ALL));
    m.put("R-D015-F2", textIs("PYG", "The currency must be PYG (D015)"));

    // Document type groups.
    m.put(
        "R-E501-D1",
        otherEquals(
            "D206", "D101", "The receiver RUC (D206) must equal the" + " issuer RUC (D101)"));
    m.put("R-E601-D1", textIs("1", "The operation condition must be cash (E601 = 1)"));
    m.put(
        "R-E606-D1",
        otherIs("E011", "5", "The presence indicator" + " (E011) must be bank operation (5)"));
    m.put("R-E310-D1", location("E310", "E312", "E314", LocationCheck.ALL));
    m.put("R-E317-D1", location("E317", "E319", "E321", LocationCheck.ALL));
    m.put("R-E925-D1", location("E925", "E927", "E929", LocationCheck.ALL));
    m.put("R-E929-D1", location("E925", "E927", "E929", LocationCheck.CITY_DEPARTMENT));
    m.put("R-E929-D2", location("E925", "E927", "E929", LocationCheck.CITY_DISTRICT));
    m.put("R-E945-D1", location("E945", "E947", "E949", LocationCheck.ALL));
    m.put("R-E949-D1", location("E945", "E947", "E949", LocationCheck.CITY_DEPARTMENT));
    m.put("R-E949-D2", location("E945", "E947", "E949", LocationCheck.CITY_DISTRICT));
    m.put("R-E625-F1", checkDigitOf("E624"));
    m.put("R-E984-F1", checkDigitOf("E983"));
    m.put("R-E996-F1", checkDigitOf("E995"));
    m.put("R-E965-F0", length(6, 6));
    m.put("R-E965-F1", length(6, 7));
    // Free description informed when the code is "Otro": only that it is not blank.
    m.put("R-E012-D2", notBlank());
    m.put("R-E502-D2", notBlank());
    m.put("R-E607-D2", notBlank());

    // Items.
    m.put("R-E701-F1", length(1, 20));
    m.put("R-E701-F2", length(1, 50));
    m.put("R-E708-F1", length(1, 120));
    m.put("R-E708-F2", length(1, 2000));
    m.put("R-E706-F1", gtin());
    m.put("R-E707-F1", gtin());
    m.put("R-E711-F1", quantity(4));
    m.put("R-E711-F2", quantity(8));
    m.put("R-E733-F1", numeric(v -> v.compareTo(HUNDRED) == 0, "Must be 100 for E731 = 1"));
    m.put("R-E733-F2", numeric(v -> v.signum() == 0, "Must be 0 for E731 = 2 or 3"));
    m.put(
        "R-E733-F3",
        numeric(
            v -> v.signum() > 0 && v.compareTo(HUNDRED) < 0,
            "Must be greater than 0 and less than 100 for E731 = 4"));
    m.put("R-E734-F1", numeric(v -> v.signum() == 0, "Must be 0 for E731 = 2 or 3"));
    m.put(
        "R-E734-F2",
        numeric(
            v -> v.compareTo(BigDecimal.valueOf(5)) == 0 || v.compareTo(BigDecimal.TEN) == 0,
            "Must be 5 or 10 for E731 = 1 or 4"));
    m.put("R-E759-F1", length(20, 20));
    m.put("R-E759-F2", length(1, 20));
    m.put("R-E760-F1", length(20, 20));
    m.put("R-E760-F2", length(1, 20));
    m.put("R-E773-F1", length(17, 17));
    m.put("R-E786-F1", length(4, 4));

    // Associated documents.
    m.put("R-H001-F1", ValueChecks::singleAssociatedDocument);
    m.put("R-H002-D1", textIs("3", "The associated document type must be electronic proof (3)"));
    m.put(
        "R-H002-D2",
        textIsNot("3", "The associated document type must not be electronic" + " proof (3)"));

    return Map.copyOf(m);
  }

  private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

  // --- generic checks ---------------------------------------------------------------------------

  private static Check textIs(String expected, String message) {
    return (e, r, t) -> expected.equals(t.text()) ? null : message;
  }

  private static Check textIsNot(String forbidden, String message) {
    return (e, r, t) -> forbidden.equals(t.text()) ? message : null;
  }

  private static Check textIn(Set<String> allowed, String message) {
    return (e, r, t) -> allowed.contains(t.text()) ? null : message;
  }

  private static Check notBlank() {
    return (e, r, t) -> {
      String v = t.text();
      return v == null || v.isBlank() ? "Must not be blank" : null;
    };
  }

  /** Another field (as seen from the target) must have a value; null when it is absent. */
  private static Check otherIs(String fieldId, String expected, String message) {
    return (e, r, t) -> {
      String v = e.text(t, fieldId);
      return v == null || expected.equals(v) ? null : message;
    };
  }

  /** Two other fields must be equal; not checked when one is absent. */
  private static Check otherEquals(String a, String b, String message) {
    return (e, r, t) -> {
      String va = e.text(t, a);
      String vb = e.text(t, b);
      return va == null || vb == null || va.equals(vb) ? null : message;
    };
  }

  private static Check sameAs(String fieldId, String message) {
    return (e, r, t) -> {
      String other = e.text(t, fieldId);
      String v = t.text();
      return other == null || v == null || other.equals(v) ? null : message;
    };
  }

  private static Check numeric(Predicate<BigDecimal> ok, String message) {
    return (e, r, t) -> {
      BigDecimal v = Evaluation.decimal(t.text());
      return v == null || ok.test(v) ? null : message;
    };
  }

  private static Check length(int min, int max) {
    String message =
        min == max
            ? "Must have exactly " + min + " characters"
            : "Must have between " + min + " and " + max + " characters";
    return (e, r, t) -> {
      String v = t.text();
      if (v == null) {
        return null;
      }
      int n = v.codePointCount(0, v.length());
      return n < min || n > max ? message : null;
    };
  }

  /** Exactly the field's fixed length (MT150 "completar con 0 a la izquierda"). */
  private static Check fixedLength() {
    return (e, r, t) -> {
      Field f = e.engine().field(t.fieldId());
      String v = t.text();
      if (f == null || f.length() == null || v == null || !DIGITS.matcher(f.length()).matches()) {
        return null;
      }
      int n = Integer.parseInt(f.length());
      return v.codePointCount(0, v.length()) == n
          ? null
          : "Must have exactly " + n + " characters, padded with zeros on the left";
    };
  }

  private static Check dateTime() {
    return (e, r, t) -> {
      String v = t.text();
      if (v == null) {
        return null;
      }
      boolean ok = DATE_TIME.matcher(v).matches();
      if (ok) {
        try {
          LocalDateTime.parse(v);
        } catch (DateTimeParseException ex) {
          ok = false;
        }
      }
      return ok ? null : "Must be a date and time in the format yyyy-MM-ddTHH:mm:ss";
    };
  }

  private static Check date() {
    return (e, r, t) -> {
      String v = t.text();
      if (v == null) {
        return null;
      }
      return DATE.matcher(v).matches() && parseDate(v) != null
          ? null
          : "Must be a date in the format yyyy-MM-dd";
    };
  }

  private static Check dateNotAfter(String fieldId, String limitId, String message) {
    return (e, r, t) -> {
      LocalDate v = dateOf(e, t, fieldId);
      LocalDate limit = dateOf(e, t, limitId);
      return v == null || limit == null || !v.isAfter(limit) ? null : message;
    };
  }

  private static Check dateNotBefore(String fieldId, String limitId, String message) {
    return (e, r, t) -> {
      LocalDate v = dateOf(e, t, fieldId);
      LocalDate limit = dateOf(e, t, limitId);
      return v == null || limit == null || !v.isBefore(limit) ? null : message;
    };
  }

  private static @Nullable LocalDate dateOf(Evaluation e, Occurrence t, String fieldId) {
    String v = fieldId.equals(t.fieldId()) ? t.text() : e.text(t, fieldId);
    return v == null || v.length() < 10 ? null : parseDate(v.substring(0, 10));
  }

  private static @Nullable LocalDate parseDate(String v) {
    try {
      return LocalDate.parse(v);
    } catch (DateTimeParseException ex) {
      return null;
    }
  }

  private static Check checkDigitOf(String rucField) {
    return (e, r, t) -> {
      String ruc = e.text(t, rucField);
      String v = t.text();
      if (ruc == null || v == null) {
        return null;
      }
      Integer dv = CheckDigit.modulo11(ruc);
      if (dv == null) {
        return null; // letters in the RUC: C-102, not checked
      }
      Field f = e.engine().field(rucField);
      String xml = f == null ? rucField : f.xml();
      return v.equals(dv.toString())
          ? null
          : "Does not match the modulo 11 check digit of the RUC in " + xml + " (" + rucField + ")";
    };
  }

  private static Check gtin() {
    return (e, r, t) -> {
      String v = t.text();
      if (v == null) {
        return null;
      }
      int n = v.length();
      return DIGITS.matcher(v).matches() && (n == 8 || n == 12 || n == 13 || n == 14)
          ? null
          : "Must have 8, 12, 13 or 14 digits";
    };
  }

  /** Up to 10 integer digits and up to {@code decimals} decimals (MT150 E711, NT023 1.2). */
  private static Check quantity(int decimals) {
    String message = "Must have up to 10 integer digits and up to " + decimals + " decimals";
    return (e, r, t) -> {
      String v = t.text();
      if (v == null || Evaluation.decimal(v) == null) {
        return null;
      }
      String unsigned = v.startsWith("-") || v.startsWith("+") ? v.substring(1) : v;
      int dot = unsigned.indexOf('.');
      String integer = dot < 0 ? unsigned : unsigned.substring(0, dot);
      String fraction = dot < 0 ? "" : unsigned.substring(dot + 1);
      String significant = integer.replaceFirst("^0+(?=.)", "");
      return significant.length() <= 10 && fraction.length() <= decimals ? null : message;
    };
  }

  // --- specific checks --------------------------------------------------------------------------

  /** R-A002-D1 (MT150 10.1, 1000): the CDC is the concatenation of its fields plus A003. */
  private static @Nullable String cdcComposition(Evaluation e, Rule r, Occurrence t) {
    String cdc = t.text();
    String base = cdcBase(e, t);
    if (cdc == null || base == null) {
      return null;
    }
    String dv = e.text(t, "A003");
    boolean ok = cdc.length() == 44 && cdc.startsWith(base) && (dv == null || cdc.endsWith(dv));
    return ok
        ? null
        : "The CDC does not match the fields it is made of (C002, D101, D102, C005, C006, C007,"
            + " D103, D002, B002, B004 and A003)";
  }

  /** R-A003-F1 (MT150 10.2, 1003): modulo 11 of the first 43 positions of the CDC. */
  private static @Nullable String cdcCheckDigit(Evaluation e, Rule r, Occurrence t) {
    String cdc = e.text(t, "A002");
    String v = t.text();
    if (cdc == null || v == null || cdc.length() != 44) {
      return null;
    }
    Integer dv = CheckDigit.modulo11(cdc.substring(0, 43));
    if (dv == null) {
      return null;
    }
    return v.equals(dv.toString()) ? null : "Does not match the modulo 11 check digit of the CDC";
  }

  private static @Nullable String cdcBase(Evaluation e, Occurrence t) {
    String type = e.text(t, "C002");
    String ruc = e.text(t, "D101");
    String rucDv = e.text(t, "D102");
    String establishment = e.text(t, "C005");
    String point = e.text(t, "C006");
    String number = e.text(t, "C007");
    String taxpayerType = e.text(t, "D103");
    String issued = e.text(t, "D002");
    String emission = e.text(t, "B002");
    String securityCode = e.text(t, "B004");
    if (type == null
        || ruc == null
        || rucDv == null
        || establishment == null
        || point == null
        || number == null
        || taxpayerType == null
        || issued == null
        || issued.length() < 10
        || emission == null
        || securityCode == null) {
      return null;
    }
    String date = issued.substring(0, 4) + issued.substring(5, 7) + issued.substring(8, 10);
    return pad(type, 2)
        + pad(ruc, 8)
        + rucDv
        + pad(establishment, 3)
        + pad(point, 3)
        + pad(number, 7)
        + taxpayerType
        + date
        + emission
        + pad(securityCode, 9);
  }

  private static String pad(String v, int n) {
    StringBuilder sb = new StringBuilder();
    for (int i = v.length(); i < n; i++) {
      sb.append('0');
    }
    return sb.append(v).toString();
  }

  /** R-B004-F1 (MT150 10.3): 9 digits, 000000001 to 999999999. */
  private static @Nullable String securityCode(Evaluation e, Rule r, Occurrence t) {
    String v = t.text();
    if (v == null) {
      return null;
    }
    return v.length() == 9 && DIGITS.matcher(v).matches() && !"000000000".equals(v)
        ? null
        : "Must be 9 digits between 000000001 and 999999999";
  }

  private static @Nullable String issueDateAfterStart(Evaluation e, Rule r, Occurrence t) {
    LocalDate v = dateOf(e, t, t.fieldId());
    return v == null || !v.isBefore(EARLIEST_ISSUE_DATE)
        ? null
        : "The issue date must be after 22 November 2018";
  }

  /** R-D105-F1 (MT150 D105, 1263). */
  private static @Nullable String testIssuerName(Evaluation e, Rule r, Occurrence t) {
    String v = t.text();
    if (v == null) {
      return null;
    }
    boolean contains = v.contains(TEST_ISSUER_NAME);
    if (e.environment() == Environment.TEST) {
      return contains
          ? null
          : "In the test environment the issuer name must contain the literal required by SIFEN";
    }
    return contains ? "The test environment literal must not be used in production" : null;
  }

  /** R-H001-F1 (MT150 2415): at most one associated document. */
  private static @Nullable String singleAssociatedDocument(Evaluation e, Rule r, Occurrence t) {
    List<Occurrence> all = e.all(t.fieldId());
    return all.size() > 1 && all.get(0).anchor() != t.anchor()
        ? "The associated document group may appear only once for this document type"
        : null;
  }

  // --- catalogs ---------------------------------------------------------------------------------

  /**
   * A description field must carry the description of its code: the one required by the XSD when it
   * differs from the Manual's ({@link CatalogEntry#descriptionToEmit()}). A code that admits a free
   * description only requires a non-blank text. Unknown codes are reported by the catalog
   * verification, not here.
   */
  static Check description(String codeField, String catalogId) {
    return (e, r, t) -> {
      String code = e.text(t, codeField);
      String v = t.text();
      if (code == null || v == null) {
        return null;
      }
      List<CatalogEntry> entries = e.engine().entries(catalogId, code);
      if (entries.isEmpty()) {
        return null;
      }
      for (CatalogEntry entry : entries) {
        if (entry.freeDescription() ? !v.isBlank() : entry.descriptionToEmit().equals(v)) {
          return null;
        }
      }
      Field f = e.engine().field(codeField);
      String xml = f == null ? codeField : f.xml();
      return "Does not match the catalog description of the code in "
          + xml
          + " ("
          + codeField
          + ")";
    };
  }

  /** What a location rule checks among department, district and city codes. */
  enum LocationCheck {
    /** District within the department, and city within the district (or the department). */
    ALL,
    /** The city belongs to the department. */
    CITY_DEPARTMENT,
    /** The city belongs to the district, when a district is informed. */
    CITY_DISTRICT
  }

  /**
   * Hierarchy of the department, district and city catalogs ({@code padre} of each entry). Codes
   * not in the catalogs are left to the catalog verification.
   */
  static Check location(String department, String district, String city, LocationCheck what) {
    return (e, r, t) -> {
      String dep = e.text(t, department);
      String dis = e.text(t, district);
      String cit = e.text(t, city);
      String districtOfCity = cit == null ? null : parentOf(e, "ciudad", cit);
      String departmentOfDistrict = dis == null ? null : parentOf(e, "distrito", dis);
      String departmentOfCity =
          districtOfCity == null ? null : parentOf(e, "distrito", districtOfCity);
      boolean districtOk =
          dep == null || departmentOfDistrict == null || dep.equals(departmentOfDistrict);
      boolean cityInDistrict = dis == null || districtOfCity == null || dis.equals(districtOfCity);
      boolean cityInDepartment =
          dep == null || departmentOfCity == null || dep.equals(departmentOfCity);
      switch (what) {
        case CITY_DEPARTMENT:
          return cityInDepartment ? null : "The city does not belong to the department";
        case CITY_DISTRICT:
          return cityInDistrict ? null : "The city does not belong to the district";
        default:
          return districtOk && cityInDistrict && cityInDepartment
              ? null
              : "The department, district and city codes are not related";
      }
    };
  }

  private static @Nullable String parentOf(Evaluation e, String catalogId, String code) {
    List<CatalogEntry> entries = e.engine().entries(catalogId, code);
    return entries.isEmpty() ? null : entries.get(0).parent();
  }
}
