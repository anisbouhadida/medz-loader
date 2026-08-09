package dz.anisbouhadida.medzloader.batch.config.reader;

import dz.anisbouhadida.medzloader.batch.dto.MedicineLine;
import dz.anisbouhadida.medzloader.batch.reader.FileAwareMedicineItemReader;
import dz.anisbouhadida.medzloader.batch.support.classifier.MedicineItemReaderClassifier;
import dz.anisbouhadida.medzloader.batch.support.factory.MedicineItemReaderFactory;
import dz.anisbouhadida.medzloader.batch.support.properties.MedzLoaderProperties;
import java.io.IOException;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.infrastructure.item.file.MultiResourceItemReader;
import org.springframework.batch.infrastructure.item.file.ResourceAwareItemReaderItemStream;
import org.springframework.batch.infrastructure.item.file.builder.MultiResourceItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/// Spring configuration that assembles the reader pipeline for medicine CSV files.
///
/// It wires together a step-scoped [MultiResourceItemReader] that scans the CSV files in one
/// release directory. The `release` job parameter names a direct child of the directory configured
/// by `medz.loader.input-dir`. The reader delegates each file to a [FileAwareMedicineItemReader],
/// which selects the correct column layout via a [MedicineItemReaderClassifier].
///
/// The release parameter contract is validated before this reader is created.
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

  /// Resource pattern used to discover CSV files directly inside a release directory.
  ///
  /// The placeholders receive the configured input directory and the release directory supplied
  /// as a job parameter. The non-recursive pattern ensures one job execution processes one release
  /// and no nested directories.
  ///
  /// @see PathMatchingResourcePatternResolver
  private static final String CSV_PATTERN = "file:%s/%s/*.csv";

  /// Typed configuration properties bound to the `medz.loader` namespace,
  /// injected by Lombok's `@RequiredArgsConstructor`.
  ///
  /// @see MedzLoaderProperties
  private final MedzLoaderProperties properties;

  /// Creates a step-scoped [MultiResourceItemReader] for one release directory.
  ///
  /// The `release` job parameter must name a direct child directory of `medz.loader.input-dir`.
  /// Only CSV files directly inside that directory are fed to the
  /// [#fileAwareMedicineItemReader(MedicineItemReaderClassifier)] delegate.
  ///
  /// @param fileAwareMedicineItemReader the resource-aware delegate reader that processes each
  /// individual CSV file
  /// @param releaseDirectory the release directory name supplied by the `release` job parameter
  /// @return a multi-resource reader spanning the selected release's CSV files
  /// @throws IOException if the resource pattern cannot be resolved
  @Bean
  @StepScope
  public MultiResourceItemReader<MedicineLine> releaseCsvItemReader(
      ResourceAwareItemReaderItemStream<MedicineLine> fileAwareMedicineItemReader,
      @Value("#{jobParameters['release']}") String releaseDirectory)
      throws IOException {
    var pattern = CSV_PATTERN.formatted(Path.of(properties.inputDir()), releaseDirectory);
    var resolver = new PathMatchingResourcePatternResolver();
    var resources = resolver.getResources(pattern);

    return new MultiResourceItemReaderBuilder<MedicineLine>()
        .name("releaseCsvItemReader")
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
  /// @param medicineItemReaderClassifier the classifier that maps a CSV filename to its
  /// corresponding flat-file reader
  /// @return a resource-aware item reader that routes to the right delegate
  @Bean
  protected ResourceAwareItemReaderItemStream<MedicineLine> fileAwareMedicineItemReader(
      MedicineItemReaderClassifier medicineItemReaderClassifier) {
    return new FileAwareMedicineItemReader(medicineItemReaderClassifier);
  }

  /// Creates the [MedicineItemReaderClassifier] responsible for mapping a
  /// CSV filename to its corresponding
  // [org.springframework.batch.infrastructure.item.file.FlatFileItemReader].
  ///
  /// @param medicineItemReaderFactory the factory used to build pre-configured flat-file readers
  /// for each CSV format
  /// @return the classifier instance
  @Bean
  protected MedicineItemReaderClassifier medicineItemReaderClassifier(
      MedicineItemReaderFactory medicineItemReaderFactory) {
    return new MedicineItemReaderClassifier(medicineItemReaderFactory);
  }

  /// Creates the [MedicineItemReaderFactory] that builds pre-configured
  /// [org.springframework.batch.infrastructure.item.file.FlatFileItemReader]] instances for each
  // supported CSV format.
  ///
  /// @return a new [MedicineItemReaderFactory] instance ready to produce format-specific readers
  @Bean
  protected MedicineItemReaderFactory medicineItemReaderFactory() {
    return new MedicineItemReaderFactory();
  }
}
