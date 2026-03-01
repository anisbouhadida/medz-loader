package dz.anisbouhadida.medzloader.batch.config;

import dz.anisbouhadida.medzloader.batch.dto.MedicineLine;
import dz.anisbouhadida.medzloader.batch.reader.FileAwareMedicineItemReader;
import dz.anisbouhadida.medzloader.batch.reader.MedicineItemReaderClassifier;
import dz.anisbouhadida.medzloader.batch.reader.MedicineItemReaderFactory;
import dz.anisbouhadida.medzloader.config.MedzLoaderProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.MultiResourceItemReader;
import org.springframework.batch.infrastructure.item.file.ResourceAwareItemReaderItemStream;
import org.springframework.batch.infrastructure.item.file.builder.MultiResourceItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.nio.file.Path;

/// Spring configuration that assembles the reader pipeline for medicine CSV files.
///
/// It wires together a [MultiResourceItemReader] that scans all CSV files under
/// the directory configured by the `medz.loader.input-dir` application property
/// and delegates the actual reading to a [FileAwareMedicineItemReader], which in
/// turn selects the correct column layout via a [MedicineItemReaderClassifier].
///
/// The `file:` prefix and the `/**/*.csv` glob are appended automatically;
/// the user only needs to supply the base directory path.
///
/// @author Anis Bouhadida
/// @since 0.0.1
/// @version 0.1.0
/// @see MultiResourceItemReader
/// @see FileAwareMedicineItemReader
/// @see MedicineItemReaderClassifier
/// @see MedicineItemReaderFactory
@Configuration
@RequiredArgsConstructor
public class MedicineItemReaderConfiguration {

    /// Ant-style resource pattern used to discover all CSV files under the configured
    /// input directory.
    ///
    /// The `%s` placeholder is replaced at runtime by the normalized directory path
    /// supplied via `medz.loader.input-dir`. For example, given an input directory of
    /// `/data/medz`, the resolved pattern becomes `file:/data/medz/**/*.csv`.
    ///
    /// @see PathMatchingResourcePatternResolver
    private static final String CSV_PATTERN = "file:%s/**/*.csv";

    /// Typed configuration properties bound to the `medz.loader` namespace,
    /// injected by Lombok's `@RequiredArgsConstructor`.
    ///
    /// @see MedzLoaderProperties
    private final MedzLoaderProperties properties;

    /// Creates a [MultiResourceItemReader] that discovers every `*.csv` file
    /// under the directory defined by `medz.loader.input-dir` and feeds
    /// them one by one to the [#fileAwareMedicineItemReader()] delegate.
    ///
    /// @param fileAwareMedicineItemReader the resource-aware delegate reader that processes each individual CSV file
    /// @return a multi-resource reader spanning all input CSV files
    /// @throws IOException if the resource pattern cannot be resolved
    @Bean
    public MultiResourceItemReader<MedicineLine> multiCsvItemReader(
            ResourceAwareItemReaderItemStream<MedicineLine> fileAwareMedicineItemReader
    ) throws IOException {
        var pattern = CSV_PATTERN.formatted(Path.of(properties.inputDir()));
        var resolver = new PathMatchingResourcePatternResolver();
        var resources = resolver.getResources(pattern);

        return new MultiResourceItemReaderBuilder<MedicineLine>()
                .name("multiCsvItemReader")
                .delegate(fileAwareMedicineItemReader)
                .resources(resources)
                .build();
    }

    /// Creates the [FileAwareMedicineItemReader] that acts as the delegate
    /// for the multi-resource reader.
    ///
    /// It dynamically resolves the correct flat-file reader for each resource
    /// by consulting the [MedicineItemReaderClassifier].
    ///
    /// @param medicineItemReaderClassifier the classifier that maps a CSV filename to its corresponding flat-file reader
    /// @return a resource-aware item reader that routes to the right delegate
    @Bean
    protected ResourceAwareItemReaderItemStream<MedicineLine> fileAwareMedicineItemReader(MedicineItemReaderClassifier medicineItemReaderClassifier) {
        return new FileAwareMedicineItemReader(medicineItemReaderClassifier);
    }

    /// Creates the [MedicineItemReaderClassifier] responsible for mapping a
    /// CSV filename to its corresponding [FlatFileItemReader].
    ///
    /// @param medicineItemReaderFactory the factory used to build pre-configured flat-file readers for each CSV format
    /// @return the classifier instance
    @Bean
    protected MedicineItemReaderClassifier medicineItemReaderClassifier( MedicineItemReaderFactory medicineItemReaderFactory) {
        return new MedicineItemReaderClassifier(medicineItemReaderFactory);
    }

    /// Creates the [MedicineItemReaderFactory] that builds pre-configured
    /// [FlatFileItemReader] instances for each supported CSV format.
    ///
    /// @return a new [MedicineItemReaderFactory] instance ready to produce format-specific readers
    @Bean
    protected MedicineItemReaderFactory medicineItemReaderFactory() {
        return new MedicineItemReaderFactory();
    }
}
