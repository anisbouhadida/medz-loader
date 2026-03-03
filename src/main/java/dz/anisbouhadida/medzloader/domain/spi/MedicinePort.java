package dz.anisbouhadida.medzloader.domain.spi;

/// SPI port for medicine persistence operations.
///
/// Infrastructure adapters (e.g. JDBC, JPA) implement this interface to provide
/// the domain layer with read access to medicine data without coupling it to any
/// specific persistence technology.
///
/// @author Anis Bouhadida
/// @since 0.2.0
public interface MedicinePort {

    /// Retrieves the current version of the medicine in the database, if it exists.
    ///
    /// @param registrationNumber the official registration number of the medicine to look up
    /// @param code the medicine code to look up
    /// @param internationalCommonDenomination the International Common Denomination (INN) to look up
    /// @param brandName the commercial brand name to look up
    /// @param laboratoryHolder the laboratory holding the registration decision to look up
    /// @return the current optimistic-locking version of the medicine, or `0` if no matching row exists
    int getMedicineVersionByRegistrationNumber(String registrationNumber, String code, String internationalCommonDenomination, String brandName, String laboratoryHolder);
}
