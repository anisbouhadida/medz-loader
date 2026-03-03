package dz.anisbouhadida.medzloader.batch.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

/// Represents a single entry from the **retraits CSV file** (medicine withdrawals).
///
/// Each entry corresponds to one row in the official Algerian medicine withdrawal list,
/// as published by the Ministry of Health.
///
/// This record is intentionally kept isolated from the domain model.
/// Date fields are kept as raw strings because
/// [org.springframework.batch.infrastructure.item.file.mapping.RecordFieldSetMapper]
/// enforces non-null conversion results, making it incompatible with blank date cells
/// in the source CSV files. String-based fields are mapped to domain types in the processor.
/// Common fields are defined in [MedicineLine].
///
/// ## Validation
/// Mandatory fields are annotated with Jakarta Bean Validation constraints.
///
/// @param id                                   sequential row number in the source file
/// @param registrationNumber                   official registration number (e.g. `156/01 A
/// 001/99`)
/// @param code                                 alphanumeric medicine code (e.g. `01 A 001`)
/// @param internationalCommonName              INN — International Non-proprietary Name
/// @param brandName                            commercial brand name of the medicine
/// @param form                                 pharmaceutical form (e.g. _COMPRIMÉ_, _GÉLULE_) —
/// may be blank
/// @param dosage                               dosage strength (e.g. `10MG`) — may be blank
/// @param packaging                            packaging description (e.g. `B/20`) — may be blank
/// @param list                                 regulatory list (e.g. `Liste II`) — may be blank
/// @param p1                                   optional first price indicator
/// @param p2                                   optional second price indicator
/// @param registrationHolderLaboratory         name of the laboratory holding the registration
/// decision
/// @param registrationHolderLaboratoryCountry  country of the registration holder laboratory
/// @param initialRegistrationDate              date the registration was first granted as raw
/// string — may be blank
/// @param type                                 medicine type as raw string (`GE`/`G` for generic,
/// `RE`/`R` for reference, or blank)
/// @param status                               registration origin as raw string (`F` for
/// finalized, `I` for in progress)
/// @param withdrawalDate                       date of withdrawal as raw string — may be an ISO
/// date, a localized text, or blank
/// @param withdrawalReason                     reason for the withdrawal — may be blank (3 rows)
public record WithdrawalLine(
    @NotNull @Positive Integer id,
    @Pattern(regexp = ".+", message = "registrationNumber must not be blank")
        String registrationNumber,
    @Pattern(regexp = ".+", message = "code must not be blank") String code,
    @Pattern(regexp = ".+", message = "internationalCommonName must not be blank")
        String internationalCommonName,
    @Pattern(regexp = ".+", message = "brandName must not be blank") String brandName,
    String form,
    String dosage,
    String packaging,
    String list,
    String p1,
    String p2,
    @Pattern(regexp = ".+", message = "registrationHolderLaboratory must not be blank")
        String registrationHolderLaboratory,
    @Pattern(regexp = ".+", message = "registrationHolderLaboratoryCountry must not be blank")
        String registrationHolderLaboratoryCountry,
    String initialRegistrationDate,
    @Pattern(regexp = "^(GE|RE|G|R|BIO)?$", message = "type must be GE, RE, G, R, BIO or blank")
        String type,
    @Pattern(regexp = "^[FIi]$", message = "origin must be F, I or i") String status,
    String withdrawalDate,
    String withdrawalReason)
    implements MedicineLine {}
