package dz.anisbouhadida.medzloader.batch.support.discovery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dz.anisbouhadida.medzloader.batch.model.MedicineRelease;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("MedicineReleaseDiscovery")
class MedicineReleaseDiscoveryTest {

  @TempDir Path inputDirectory;

  @Nested
  @DisplayName("discover()")
  class DiscoverTests {

    @Test
    void discover_shouldReturnReleasesInChronologicalOrder() throws IOException {
      createRelease("2026-04");
      createRelease("2026-02");

      var releases = new MedicineReleaseDiscovery(inputDirectory).discover();

      assertThat(releases)
          .extracting(MedicineRelease::version)
          .containsExactly("2026-02", "2026-04");
      assertThat(releases.getFirst().relativeDirectory()).isEqualTo(Path.of("2026-02"));
      assertThat(releases.getFirst().resources())
          .extracting(resource -> resource.getFilename())
          .containsExactly("nomenclature.csv", "non_renouveles.csv", "retraits.csv");
    }

    @Test
    void discover_shouldReturnEmptyList_whenInputDirectoryIsEmpty() throws IOException {
      assertThat(new MedicineReleaseDiscovery(inputDirectory).discover()).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"2026-4", "April-2026", "2026-13"})
    void discover_shouldRejectInvalidReleaseDirectoryNames(String directory) throws IOException {
      Files.createDirectory(inputDirectory.resolve(directory));

      assertThatThrownBy(() -> new MedicineReleaseDiscovery(inputDirectory).discover())
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining(directory)
          .hasMessageContaining("YYYY-MM");
    }

    @Test
    void discover_shouldRejectMissingRequiredFile() throws IOException {
      var release = Files.createDirectory(inputDirectory.resolve("2026-04"));
      Files.createFile(release.resolve("nomenclature.csv"));
      Files.createFile(release.resolve("retraits.csv"));

      assertThatThrownBy(() -> new MedicineReleaseDiscovery(inputDirectory).discover())
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("2026-04")
          .hasMessageContaining("NON_RENEWAL");
    }

    @Test
    void discover_shouldRejectDuplicateFileType() throws IOException {
      var release = createRelease("2026-04");
      Files.createFile(release.resolve("nomenclature_copy.csv"));

      assertThatThrownBy(() -> new MedicineReleaseDiscovery(inputDirectory).discover())
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("duplicate NOMENCLATURE");
    }

    @Test
    void discover_shouldRejectUnknownCsvFile() throws IOException {
      var release = createRelease("2026-04");
      Files.createFile(release.resolve("other.csv"));

      assertThatThrownBy(() -> new MedicineReleaseDiscovery(inputDirectory).discover())
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("other.csv");
    }

    @Test
    void discover_shouldRejectNonDirectoryChild() throws IOException {
      Files.createFile(inputDirectory.resolve("2026-04.csv"));

      assertThatThrownBy(() -> new MedicineReleaseDiscovery(inputDirectory).discover())
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("expected a release directory");
    }

    @Test
    void discover_shouldRejectNestedDirectory() throws IOException {
      var release = createRelease("2026-04");
      Files.createDirectory(release.resolve("nested"));

      assertThatThrownBy(() -> new MedicineReleaseDiscovery(inputDirectory).discover())
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("release entries must be CSV files");
    }
  }

  private Path createRelease(String version) throws IOException {
    var release = Files.createDirectory(inputDirectory.resolve(version));
    Files.createFile(release.resolve("nomenclature.csv"));
    Files.createFile(release.resolve("non_renouveles.csv"));
    Files.createFile(release.resolve("retraits.csv"));
    return release;
  }
}
