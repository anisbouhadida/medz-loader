package dz.anisbouhadida.medzloader.batch.reader;

import dz.anisbouhadida.medzloader.batch.dto.NomenclatureLine;
import dz.anisbouhadida.medzloader.batch.dto.NonRenewalLine;
import dz.anisbouhadida.medzloader.batch.dto.WithdrawalLine;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.infrastructure.item.file.mapping.RecordFieldSetMapper;


/// Factory responsible for creating [FlatFileItemReader] instances
/// for each type of medicine CSV file.
///
/// Each factory method builds a reader pre-configured with the correct
/// field names and line-skip settings for its target file format.
///
/// @see NomenclatureLine
/// @see WithdrawalLine
/// @see NonRenewalLine
public class MedicineItemReaderFactory {

    /// Column name constant for the medicine identifier.
    private static final String ID = "id";

    /// Column name constant for the registration number.
    private static final String REGISTRATION_NUMBER = "registrationNumber";

    /// Column name constant for the medicine code.
    private static final String CODE = "code";

    /// Column name constant for the International Common Name (INN).
    private static final String INN = "internationalCommonName";

    /// Column name constant for the brand name.
    private static final String BRAND_NAME = "brandName";

    /// Column name constant for the pharmaceutical form.
    private static final String FORM = "form";

    /// Column name constant for the dosage.
    private static final String DOSAGE = "dosage";

    /// Column name constant for the packaging.
    private static final String PACKAGING = "packaging";

    /// Column name constant for the list classification.
    private static final String LIST = "list";

    /// Column name constant for the first price field.
    private static final String P1 = "p1";

    /// Column name constant for the second price field.
    private static final String P2 = "p2";

    /// Column name constant for the registration holder laboratory.
    private static final String LAB = "registrationHolderLaboratory";

    /// Column name constant for the registration holder laboratory country.
    private static final String LAB_COUNTRY = "registrationHolderLaboratoryCountry";

    /// Column name constant for the initial registration date.
    private static final String INITIAL_REG_DATE = "initialRegistrationDate";

    /// Column name constant for the final registration date.
    private static final String FINAL_REG_DATE = "finalRegistrationDate";

    /// Column name constant for the medicine type.
    private static final String TYPE = "type";

    /// Column name constant for the medicine origin.
    private static final String STATUS = "origin";

    /// Ordered field names for the **nomenclature** CSV format.
    ///
    /// Includes all common fields plus `obs` (observations) and `stabilityDuration`.
    private static final String[] NOMENCLATURE_FIELDS = {
            ID, REGISTRATION_NUMBER, CODE, INN,
            BRAND_NAME, FORM, DOSAGE, PACKAGING, LIST, P1, P2,
            "obs", LAB, LAB_COUNTRY,
            INITIAL_REG_DATE, FINAL_REG_DATE, TYPE, STATUS, "stabilityDuration"
    };

    /// Ordered field names for the **withdrawal** CSV format.
    ///
    /// Includes all common fields plus `withdrawalDate` and `withdrawalReason`.
    private static final String[] WITHDRAWAL_FIELDS = {
            ID, REGISTRATION_NUMBER, CODE, INN,
            BRAND_NAME, FORM, DOSAGE, PACKAGING, LIST, P1, P2,
            LAB, LAB_COUNTRY,
            INITIAL_REG_DATE, TYPE, STATUS, "withdrawalDate", "withdrawalReason"
    };

    /// Ordered field names for the **non-renewal** CSV format.
    ///
    /// Includes all common fields plus `obs` (observations).
    private static final String[] NON_RENEWAL_FIELDS = {
            ID, REGISTRATION_NUMBER, CODE, INN,
            BRAND_NAME, FORM, DOSAGE, PACKAGING, LIST, P1, P2,
            "obs", LAB, LAB_COUNTRY,
            INITIAL_REG_DATE, FINAL_REG_DATE, TYPE, STATUS
    };

    /// Creates a [FlatFileItemReader] configured to read **nomenclature** CSV files.
    ///
    /// The reader skips the header line and maps each row to a [NomenclatureLine] record.
    ///
    /// @return a pre-configured reader for nomenclature data
    public FlatFileItemReader<NomenclatureLine> createNomenclatureReader() {
        return new FlatFileItemReaderBuilder<NomenclatureLine>()
                .name("nomenclatureItemReader")
                .linesToSkip(1)
                .delimited()
                .names(NOMENCLATURE_FIELDS)
                .fieldSetMapper(new RecordFieldSetMapper<>(NomenclatureLine.class))
                .build();
    }

    /// Creates a [FlatFileItemReader] configured to read **withdrawal** CSV files.
    ///
    /// The reader skips the header line and maps each row to a [WithdrawalLine] record.
    ///
    /// @return a pre-configured reader for withdrawal data
    public FlatFileItemReader<WithdrawalLine> createWithdrawalReader() {
        return new FlatFileItemReaderBuilder<WithdrawalLine>()
                .name("withdrawalItemReader")
                .linesToSkip(1)
                .delimited()
                .names(WITHDRAWAL_FIELDS)
                .fieldSetMapper(new RecordFieldSetMapper<>(WithdrawalLine.class))
                .build();
    }

    /// Creates a [FlatFileItemReader] configured to read **non-renewal** CSV files.
    ///
    /// The reader skips the header line and maps each row to a [NonRenewalLine] record.
    ///
    /// @return a pre-configured reader for non-renewal data
    public FlatFileItemReader<NonRenewalLine> createNonRenewalReader() {
        return new FlatFileItemReaderBuilder<NonRenewalLine>()
                .name("nonRenewalItemReader")
                .linesToSkip(1)
                .delimited()
                .names(NON_RENEWAL_FIELDS)
                .fieldSetMapper(new RecordFieldSetMapper<>(NonRenewalLine.class))
                .build();
    }
}


