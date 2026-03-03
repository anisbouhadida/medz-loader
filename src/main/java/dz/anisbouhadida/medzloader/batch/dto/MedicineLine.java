package dz.anisbouhadida.medzloader.batch.dto;

/// Common contract for all **medicine CSV entries**.
///
/// This sealed interface defines the fields shared across every source file
/// (nomenclature, withdrawals, non-renewals). Concrete records implement this
/// interface and add their own file-specific fields.
///
/// ## Permitted implementations
/// - [NomenclatureLine] — from `nomenclature.csv`
/// - [WithdrawalLine]   — from `retraits.csv`
/// - [NonRenewalLine]   — from `non_renouveles.csv`
///
/// @see NomenclatureLine
/// @see WithdrawalLine
/// @see NonRenewalLine
public sealed interface MedicineLine permits NomenclatureLine, WithdrawalLine, NonRenewalLine {

  /// Sequential row number in the source file.
  Integer id();

  /// Official registration number (e.g. `035/01 A 003/17/23`).
  String registrationNumber();

  /// Alphanumeric medicine code (e.g. `01 A 003`).
  String code();

  /// INN — International Non-proprietary Name.
  String internationalCommonName();

  /// Commercial brand name of the medicine.
  String brandName();

  /// Pharmaceutical form (e.g. _COMPRIMÉ PELLICULÉ SÉCABLE_).
  String form();

  /// Dosage strength (e.g. `10MG`).
  String dosage();

  /// Packaging description (e.g. `B/15`).
  String packaging();

  /// Regulatory list (e.g. `LISTE II`).
  String list();

  /// Optional first price indicator.
  String p1();

  /// Optional second price indicator.
  String p2();

  /// Name of the laboratory holding the registration decision.
  String registrationHolderLaboratory();

  /// Country of the registration holder laboratory.
  String registrationHolderLaboratoryCountry();

  /// Date the registration was first granted as raw string (`yyyy-MM-dd HH:mm:ss`) — may be blank.
  /// Parsing to [java.time.LocalDateTime] is deferred to the processor.
  String initialRegistrationDate();

  /// Medicine type as raw string (e.g. `GE` for generic, `RE` for reference).
  String type();

  /// Registration origin as raw string (e.g. `F` for finalized, `I` for in progress).
  String status();
}
