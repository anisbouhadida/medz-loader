package dz.anisbouhadida.medzloader.batch.support.mapper;

import dz.anisbouhadida.medzloader.batch.dto.MedicineLine;
import dz.anisbouhadida.medzloader.batch.dto.NomenclatureLine;
import dz.anisbouhadida.medzloader.batch.dto.NonRenewalLine;
import dz.anisbouhadida.medzloader.batch.dto.WithdrawalLine;
import dz.anisbouhadida.medzloader.domain.model.Medicine;
import dz.anisbouhadida.medzloader.domain.model.NomenclatureEvent;
import dz.anisbouhadida.medzloader.domain.model.NonRenewalEvent;
import dz.anisbouhadida.medzloader.domain.model.WithdrawalEvent;
import dz.anisbouhadida.medzloader.domain.model.enums.MedicineOrigin;
import dz.anisbouhadida.medzloader.domain.model.enums.MedicineType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Mapper
public interface MedicineLineMapper {

    DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Named("toMedicine")
    @Mapping(target = "registrationNumber", expression = "java(line.registrationNumber())")
    @Mapping(target = "code", expression = "java(line.code())")
    @Mapping(target = "internationalCommonDenomination", expression = "java(line.internationalCommonName())")
    @Mapping(target = "brandName", expression = "java(line.brandName())")
    @Mapping(target = "form", expression = "java(line.form())")
    @Mapping(target = "dosage", expression = "java(line.dosage())")
    @Mapping(target = "packaging", expression = "java(line.packaging())")
    @Mapping(target = "list", expression = "java(line.list())")
    @Mapping(target = "p1", expression = "java(line.p1())")
    @Mapping(target = "p2", expression = "java(line.p2())")
    @Mapping(target = "laboratoryHolder", expression = "java(line.registrationHolderLaboratory())")
    @Mapping(target = "laboratoryCountry", expression = "java(line.registrationHolderLaboratoryCountry())")
    @Mapping(target = "initialRegistrationDate", expression = "java(parseDate(line.initialRegistrationDate()))")
    @Mapping(target = "type", expression = "java(toMedicineType(line.type()))")
    @Mapping(target = "origin", expression = "java(toMedicineOrigin(line.status()))")
    Medicine toMedicine(MedicineLine line);

    @Mapping(target = "medicine", source = ".", qualifiedByName = "toMedicine")
    @Mapping(target = "observations", source = "obs")
    NomenclatureEvent toMedicineEvent(NomenclatureLine line);

    @Mapping(target = "medicine", source = ".", qualifiedByName = "toMedicine")
    WithdrawalEvent toMedicineEvent(WithdrawalLine line);

    @Mapping(target = "medicine", source = ".", qualifiedByName = "toMedicine")
    @Mapping(target = "observations", source = "obs")
    NonRenewalEvent toMedicineEvent(NonRenewalLine line);

    @Deprecated(forRemoval = true)
    default LocalDateTime parseDate(String dateStr) {

        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateStr, DATE_FORMATTER);
        } catch (Exception _) {
            return null;
        }
    }

    default MedicineType toMedicineType(String typeStr) {
        return switch (typeStr) {
            case "GE", "G" -> MedicineType.GE;
            case "RE", "R" -> MedicineType.RE;
            case "BIO" -> MedicineType.BIO;
            default -> null;
        };
    }

    default MedicineOrigin toMedicineOrigin(String statusStr) {
        return switch (statusStr) {
            case "F" -> MedicineOrigin.MANUFACTURED;
            case "I" -> MedicineOrigin.IMPORTED;
            default -> null;
        };
    }


}
