package dz.anisbouhadida.medzloader.batch.config.reader;

import static org.assertj.core.api.Assertions.assertThat;

import dz.anisbouhadida.medzloader.batch.dto.MedicineLine;
import dz.anisbouhadida.medzloader.batch.support.properties.MedzLoaderProperties;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.item.file.ResourceAwareItemReaderItemStream;
import org.springframework.core.io.Resource;

@DisplayName("MedicineItemReaderConfiguration")
class MedicineItemReaderConfigurationTest {

  @TempDir Path inputDirectory;

  private MedicineItemReaderConfiguration configuration;

  @BeforeEach
  void setUp() {
    var properties =
        new MedzLoaderProperties(inputDirectory.toString(), ZoneId.of("Africa/Algiers"));
    configuration = new MedicineItemReaderConfiguration(properties);
  }

  @Test
  @DisplayName("should read only CSV files directly inside the requested release")
  void shouldReadOnlyCsvFilesInsideRequestedRelease() throws Exception {
    var requestedRelease = Files.createDirectory(inputDirectory.resolve("2026-04"));
    Files.createFile(requestedRelease.resolve("nomenclature.csv"));
    Files.createFile(requestedRelease.resolve("retraits.csv"));
    Files.createFile(requestedRelease.resolve("notes.txt"));
    var nestedDirectory = Files.createDirectory(requestedRelease.resolve("nested"));
    Files.createFile(nestedDirectory.resolve("non_renouveles.csv"));

    var otherRelease = Files.createDirectory(inputDirectory.resolve("2026-05"));
    Files.createFile(otherRelease.resolve("non_renouveles.csv"));

    var delegate = new RecordingItemReader();
    var reader = configuration.releaseCsvItemReader(delegate, "2026-04");
    reader.open(new ExecutionContext());
    reader.read();
    reader.close();

    assertThat(delegate.filenames).containsExactlyInAnyOrder("nomenclature.csv", "retraits.csv");
  }

  private static final class RecordingItemReader
      implements ResourceAwareItemReaderItemStream<MedicineLine> {

    private final List<String> filenames = new ArrayList<>();

    @Override
    public void setResource(Resource resource) {
      filenames.add(resource.getFilename());
    }

    @Override
    public MedicineLine read() {
      return null;
    }

    @Override
    public void open(ExecutionContext executionContext) {}

    @Override
    public void update(ExecutionContext executionContext) {}

    @Override
    public void close() {}
  }
}
