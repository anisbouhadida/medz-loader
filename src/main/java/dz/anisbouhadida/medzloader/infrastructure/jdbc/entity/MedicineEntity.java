package dz.anisbouhadida.medzloader.infrastructure.jdbc.entity;

import dz.anisbouhadida.medzloader.domain.model.enums.MedicineOrigin;
import dz.anisbouhadida.medzloader.domain.model.enums.MedicineType;

import java.time.OffsetDateTime;

/// Represents a row from the `medicine` table.
///
/// This record is an infrastructure-layer projection used exclusively for JDBC
/// read operations (e.g. retrieving the current `version` for optimistic-locking
/// checks before a batch upsert).
///
/// It must **not** be exposed outside the infrastructure layer; map it to a domain
/// object before crossing layer boundaries.
///
/// Column mapping:
///
/// | DB column                    | Record component             |
/// |------------------------------|------------------------------|
/// | medicine_id                  | id                           |
/// | registration_number          | registrationNumber           |
/// | code                         | code                         |
/// | icd                          | icd                          |
/// | brand_name                   | brandName                    |
/// | form                         | form                         |
/// | dosage                       | dosage                       |
/// | packaging                    | packaging                    |
/// | list                         | list                         |
/// | p1                           | p1                           |
/// | p2                           | p2                           |
/// | laboratory_holder            | laboratoryHolder             |
/// | laboratory_country           | laboratoryCountry            |
/// | initial_registration_date    | initialRegistrationDate      |
/// | type                         | type                         |
/// | origin                       | origin                       |
/// | version                      | version                      |
/// | last_updated                 | lastUpdated                  |
///
/// @param id                      surrogate primary key ({@code medicine_id})
/// @param registrationNumber      official registration number — never blank
/// @param code                    unique medicine code — nullable
/// @param icd                     International Common Denomination — nullable
/// @param brandName               commercial brand name — nullable
/// @param form                    pharmaceutical form — nullable
/// @param dosage                  dosage — nullable
/// @param packaging               packaging description — nullable
/// @param list                    classification list — nullable
/// @param p1                      first price indicator — nullable
/// @param p2                      second price indicator — nullable
/// @param laboratoryHolder        laboratory holding the registration — nullable
/// @param laboratoryCountry       country of the laboratory holder — nullable
/// @param initialRegistrationDate date the registration was first granted — nullable
/// @param type                    regulatory type of the medicine — nullable
/// @param origin                  manufacturing origin of the medicine — nullable
/// @param version                 optimistic-locking version counter
/// @param lastUpdated             timestamp of the last update
///
/// @author Anis Bouhadida
/// @since 0.2.0
public record MedicineEntity(
        long id,
        String registrationNumber,
        String code,
        String icd,
        String brandName,
        String form,
        String dosage,
        String packaging,
        String list,
        String p1,
        String p2,
        String laboratoryHolder,
        String laboratoryCountry,
        OffsetDateTime initialRegistrationDate,
        MedicineType type,
        MedicineOrigin origin,
        int version,
        OffsetDateTime lastUpdated
) {}
