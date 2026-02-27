package dz.anisbouhadida.medzloader.domain.model;

import dz.anisbouhadida.medzloader.domain.model.enums.MedicineOrigin;
import dz.anisbouhadida.medzloader.domain.model.enums.MedicineType;

import java.time.LocalDateTime;

/// Represents a registered medicine with all its regulatory information.
///
/// This record is an immutable domain object that contains the official
/// registration data of a medicine, including its identifiers, composition,
/// pharmaceutical form, and administrative information.
///
/// @param registrationNumber Official registration number of the medicine
/// @param code Unique code of the medicine
/// @param internationalCommonDenomination International Common Denomination (INN)
/// @param brandName Commercial brand name
/// @param form Pharmaceutical form (tablet, solution, etc.)
/// @param dosage Dosage of the medicine
/// @param packaging Product packaging
/// @param list Classification list
/// @param p1 P1 data
/// @param p2 P2 data
/// @param laboratoryHolder Laboratory holding the registration decision
/// @param laboratoryCountry Country of the laboratory holder
/// @param initialRegistrationDate Initial registration date
/// @param type Type of medicine
/// @param origin Current origin of the medicine (e.g., national, imported)
public record Medicine(
        String registrationNumber,
        String code,
        String internationalCommonDenomination,
        String brandName,
        String form,
        String dosage,
        String packaging,
        String list,
        String p1,
        String p2,
        String laboratoryHolder,
        String laboratoryCountry,
        LocalDateTime initialRegistrationDate,
        MedicineType type,
        MedicineOrigin origin
) {}
