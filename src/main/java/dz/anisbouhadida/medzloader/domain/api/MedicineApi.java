package dz.anisbouhadida.medzloader.domain.api;

/// Entry-point API for medicine-related domain operations.
///
/// Callers outside the domain layer should depend on this interface rather than
/// concrete service classes.
///
/// @author Anis Bouhadida
/// @since 0.2.0
public interface MedicineApi {

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
