package dz.anisbouhadida.medzloader.infrastructure.jdbc.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/// JDBC implementation of [MedicineRepository].
///
/// Uses a [NamedParameterJdbcTemplate] to issue a targeted
/// `SELECT version FROM medicine WHERE <business-key>` query.
/// Only the `version` column is fetched, keeping the query lightweight
/// for high-frequency optimistic-locking checks performed during batch upserts.
///
/// The business-key lookup mirrors the `uq_medicine_business_key` unique index
/// defined in the DDL (`NULLS NOT DISTINCT`): all five key columns may be
/// `NULL` independently and are compared accordingly in SQL.
///
/// @author Anis Bouhadida
/// @since 0.2.0
@Repository
@RequiredArgsConstructor
class JdbcMedicineRepository implements MedicineRepository {

    private static final String FIND_VERSION_SQL = """
            SELECT version
            FROM   medicine
            WHERE  registration_number = :registrationNumber
              AND  (code IS NOT DISTINCT FROM :code)
              AND  (icd  IS NOT DISTINCT FROM :icd)
              AND  (brand_name IS NOT DISTINCT FROM :brandName)
              AND  (laboratory_holder IS NOT DISTINCT FROM :laboratoryHolder)
            LIMIT  1
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    /// Executes a single-column `SELECT version` against the `medicine` table
    /// using the composite business key. Returns [Optional#empty()] when no row
    /// matches (new medicine) or when the query raises
    /// [EmptyResultDataAccessException].
    @Override
    public Optional<Integer> findVersionByRegistrationNumber(
            String registrationNumber,
            String code,
            String internationalCommonDenomination,
            String brandName,
            String laboratoryHolder
    ) {
        var params = new MapSqlParameterSource()
                .addValue("registrationNumber", registrationNumber)
                .addValue("code", code)
                .addValue("icd", internationalCommonDenomination)
                .addValue("brandName", brandName)
                .addValue("laboratoryHolder", laboratoryHolder);
        try {
            var version = jdbcTemplate.queryForObject(FIND_VERSION_SQL, params, Integer.class);
            return Optional.ofNullable(version);
        } catch (EmptyResultDataAccessException _) {
            return Optional.empty();
        }
    }
}


