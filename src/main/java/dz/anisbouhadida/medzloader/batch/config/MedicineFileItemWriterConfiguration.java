package dz.anisbouhadida.medzloader.batch.config;

import dz.anisbouhadida.medzloader.batch.dto.MedicineLine;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.file.FlatFileItemWriter;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

/// Bootstrap [ItemWriter] configuration that writes [MedicineLine] records
/// to a flat file for quick verification of the batch pipeline.
/// @deprecated This is a simple implementation intended for testing and demonstration purposes, and is marked as deprecated to indicate that it should be replaced with a more robust solution in production.
@Configuration
@Deprecated(forRemoval = true)
public class MedicineFileItemWriterConfiguration {

    /// Creates a [FlatFileItemWriter] that outputs medicine data as delimited text.
    ///
    /// The output file is written to `output/medicines.csv` with a semicolon delimiter.
    ///
    /// @return a configured [ItemWriter] for [MedicineLine]
    @Bean
    public ItemWriter<MedicineLine> itemWriter() {
        return new FlatFileItemWriterBuilder<MedicineLine>()
                .name("medicineItemWriter")
                .resource(new FileSystemResource("target/output/medicines.csv"))
                .delimited()
                .delimiter(";")
                .names("id", "registrationNumber", "code", "internationalCommonName",
                        "brandName", "form", "dosage", "packaging", "list",
                        "p1", "p2", "registrationHolderLaboratory",
                        "registrationHolderLaboratoryCountry",
                        "initialRegistrationDate", "type", "origin")
                .append(true)
                .shouldDeleteIfEmpty(true)
                .shouldDeleteIfExists(true)
                .build();
    }
}

