package dz.anisbouhadida.medzloader.batch.config;

import dz.anisbouhadida.medzloader.batch.dto.MedicineLine;
import dz.anisbouhadida.medzloader.batch.reader.FileAwareMedicineItemReader;
import dz.anisbouhadida.medzloader.batch.reader.MedicineItemReaderClassifier;
import dz.anisbouhadida.medzloader.batch.reader.MedicineItemReaderFactory;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.MultiResourceItemReader;
import org.springframework.batch.infrastructure.item.file.ResourceAwareItemReaderItemStream;
import org.springframework.batch.infrastructure.item.file.builder.MultiResourceItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;

/// Spring configuration that assembles the reader pipeline for medicine CSV files.
///
/// It wires together a [MultiResourceItemReader] that scans all CSV files under
/// `src/main/resources/input/` and delegates the actual reading to a
/// [FileAwareMedicineItemReader], which in turn selects the correct column
/// layout via a [MedicineItemReaderClassifier].
@Configuration
public class MedicineItemReaderConfiguration {

    /// Creates a [MultiResourceItemReader] that discovers every `*.csv` file
    /// under `src/main/resources/input/` and feeds them one by one to the
    /// [#fileAwareMedicineItemReader()] delegate.
    ///
    /// @return a multi-resource reader spanning all input CSV files
    /// @throws IOException if the resource pattern cannot be resolved
    @Bean
    public MultiResourceItemReader<MedicineLine> multiCsvItemReader() throws IOException {
        var resolver = new PathMatchingResourcePatternResolver();
        var resources = resolver.getResources("file:src/main/resources/input/**/*.csv");

        return new MultiResourceItemReaderBuilder<MedicineLine>()
                .name("multiCsvItemReader")
                .delegate(fileAwareMedicineItemReader())
                .resources(resources)
                .build();
    }

    /// Creates the [FileAwareMedicineItemReader] that acts as the delegate
    /// for the multi-resource reader.
    ///
    /// It dynamically resolves the correct flat-file reader for each resource
    /// by consulting the [MedicineItemReaderClassifier].
    ///
    /// @return a resource-aware item reader that routes to the right delegate
    @Bean
    protected ResourceAwareItemReaderItemStream<MedicineLine> fileAwareMedicineItemReader() {
        return new FileAwareMedicineItemReader(medicineItemReaderClassifier());
    }

    /// Creates the [MedicineItemReaderClassifier] responsible for mapping a
    /// CSV filename to its corresponding [FlatFileItemReader].
    ///
    /// @return the classifier instance
    @Bean
    protected MedicineItemReaderClassifier medicineItemReaderClassifier() {
        return new MedicineItemReaderClassifier(medicineItemReaderFactory());
    }

    /// Creates the [MedicineItemReaderFactory] that builds pre-configured
    /// [FlatFileItemReader] instances for each supported CSV format.
    ///
    /// @return the reader factory instance
    @Bean
    protected MedicineItemReaderFactory medicineItemReaderFactory() {
        return new MedicineItemReaderFactory();
    }
}
