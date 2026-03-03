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
/// @param registrationNumber        official registration number of the medicine
/// @param code                      unique code of the medicine
/// @param internationalCommonDenomination international Common Denomination (INN)
/// @param brandName                 commercial brand name
/// @param form                      pharmaceutical form (tablet, solution, etc.)
/// @param dosage                    dosage of the medicine
/// @param packaging                 product packaging
/// @param list                      classification list
/// @param p1                        first price indicator
/// @param p2                        second price indicator
/// @param laboratoryHolder          laboratory holding the registration decision
/// @param laboratoryCountry         country of the laboratory holder
/// @param initialRegistrationDate   initial registration date
/// @param type                      type of medicine
/// @param origin                    current origin of the medicine (e.g. national, imported)
/// @param version                   optimistic-locking version counter
///
/// @author Anis Bouhadida
/// @since 0.0.1
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
    MedicineOrigin origin,
    int version) {}
