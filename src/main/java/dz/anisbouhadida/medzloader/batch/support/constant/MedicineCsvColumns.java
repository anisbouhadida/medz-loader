package dz.anisbouhadida.medzloader.batch.support.constant;

import dz.anisbouhadida.medzloader.batch.reader.MedicineItemReaderFactory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/// Defines the CSV column name constants and ordered field arrays
/// used when reading medicine CSV files.
///
/// Centralizing these names here avoids duplicating string literals
/// across readers and makes it easy to track column-naming changes
/// in a single place.
///
/// @see MedicineItemReaderFactory
///
/// @author Anis Bouhadida
/// @since 0.1.0
/// @version 0.1.0
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MedicineCsvColumns {

  /// Column name for the medicine identifier.
  public static final String ID = "id";

  /// Column name for the registration number.
  public static final String REGISTRATION_NUMBER = "registrationNumber";

  /// Column name for the medicine code.
  public static final String CODE = "code";

  /// Column name for the International Common Name (INN).
  public static final String INN = "internationalCommonName";

  /// Column name for the brand name.
  public static final String BRAND_NAME = "brandName";

  /// Column name for the pharmaceutical form.
  public static final String FORM = "form";

  /// Column name for the dosage.
  public static final String DOSAGE = "dosage";

  /// Column name for the packaging.
  public static final String PACKAGING = "packaging";

  /// Column name for the list classification.
  public static final String LIST = "list";

  /// Column name for the first price field.
  public static final String P1 = "p1";

  /// Column name for the second price field.
  public static final String P2 = "p2";

  /// Column name for the registration holder laboratory.
  public static final String LAB = "registrationHolderLaboratory";

  /// Column name for the registration holder laboratory country.
  public static final String LAB_COUNTRY = "registrationHolderLaboratoryCountry";

  /// Column name for the initial registration date.
  public static final String INITIAL_REG_DATE = "initialRegistrationDate";

  /// Column name for the final registration date.
  public static final String FINAL_REG_DATE = "finalRegistrationDate";

  /// Column name for the medicine type.
  public static final String TYPE = "type";

  /// Column name for the medicine status/origin.
  public static final String STATUS = "status";

  private static final String[] NOMENCLATURE_FIELDS = {
    ID,
    REGISTRATION_NUMBER,
    CODE,
    INN,
    BRAND_NAME,
    FORM,
    DOSAGE,
    PACKAGING,
    LIST,
    P1,
    P2,
    "obs",
    LAB,
    LAB_COUNTRY,
    INITIAL_REG_DATE,
    FINAL_REG_DATE,
    TYPE,
    STATUS,
    "stabilityDuration"
  };

  private static final String[] WITHDRAWAL_FIELDS = {
    ID,
    REGISTRATION_NUMBER,
    CODE,
    INN,
    BRAND_NAME,
    FORM,
    DOSAGE,
    PACKAGING,
    LIST,
    P1,
    P2,
    LAB,
    LAB_COUNTRY,
    INITIAL_REG_DATE,
    TYPE,
    STATUS,
    "withdrawalDate",
    "withdrawalReason"
  };

  private static final String[] NON_RENEWAL_FIELDS = {
    ID,
    REGISTRATION_NUMBER,
    CODE,
    INN,
    BRAND_NAME,
    FORM,
    DOSAGE,
    PACKAGING,
    LIST,
    P1,
    P2,
    "obs",
    LAB,
    LAB_COUNTRY,
    INITIAL_REG_DATE,
    FINAL_REG_DATE,
    TYPE,
    STATUS
  };

  /// Returns a copy of the ordered field names for the **nomenclature** CSV format.
  ///
  /// Includes all common fields plus `obs` (observations) and `stabilityDuration`.
  public static String[] nomenclatureFields() {
    return NOMENCLATURE_FIELDS.clone();
  }

  /// Returns a copy of the ordered field names for the **withdrawal** CSV format.
  ///
  /// Includes all common fields plus `withdrawalDate` and `withdrawalReason`.
  public static String[] withdrawalFields() {
    return WITHDRAWAL_FIELDS.clone();
  }

  /// Returns a copy of the ordered field names for the **non-renewal** CSV format.
  ///
  /// Includes all common fields plus `obs` (observations).
  public static String[] nonRenewalFields() {
    return NON_RENEWAL_FIELDS.clone();
  }
}
