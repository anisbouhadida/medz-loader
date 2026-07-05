package dz.anisbouhadida.medzloader.batch.support.discovery;

import dz.anisbouhadida.medzloader.batch.model.MedicineRelease;
import dz.anisbouhadida.medzloader.batch.support.classifier.MedicineCsvFileType;
import dz.anisbouhadida.medzloader.batch.support.properties.MedzLoaderProperties;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

/// Discovers and validates dated medicine releases under the configured input directory.
public final class MedicineReleaseDiscovery {

  private static final DateTimeFormatter RELEASE_FORMAT =
      new DateTimeFormatterBuilder()
          .appendPattern("uuuu-MM")
          .toFormatter()
          .withResolverStyle(ResolverStyle.STRICT);

  private final Path inputDirectory;

  /// Creates discovery for the configured loader input directory.
  ///
  /// @param properties loader configuration
  public MedicineReleaseDiscovery(MedzLoaderProperties properties) {
    this(Path.of(properties.inputDir()));
  }

  MedicineReleaseDiscovery(Path inputDirectory) {
    this.inputDirectory = inputDirectory.toAbsolutePath().normalize();
  }

  /// Returns complete releases sorted from oldest to newest.
  ///
  /// @return immutable chronological releases
  /// @throws IOException when the input directory cannot be inspected
  /// @throws IllegalArgumentException when the directory contract is invalid
  public List<MedicineRelease> discover() throws IOException {
    var releases = new ArrayList<MedicineRelease>();
    try (var children = Files.list(inputDirectory)) {
      for (var child : children.toList()) {
        releases.add(toRelease(child));
      }
    }
    releases.sort(Comparator.comparing(MedicineRelease::period));
    return List.copyOf(releases);
  }

  private MedicineRelease toRelease(Path directory) throws IOException {
    if (!Files.isDirectory(directory)) {
      throw invalid(directory, "expected a release directory");
    }

    var period = parsePeriod(directory);
    var resourcesByType = discoverCsvFiles(directory);
    var requiredTypes = EnumSet.allOf(MedicineCsvFileType.class);
    if (!resourcesByType.keySet().equals(requiredTypes)) {
      var missingTypes = EnumSet.copyOf(requiredTypes);
      missingTypes.removeAll(resourcesByType.keySet());
      throw invalid(directory, "missing required file types: " + missingTypes);
    }

    List<Resource> resources =
        List.of(
            new FileSystemResource(resourcesByType.get(MedicineCsvFileType.NOMENCLATURE)),
            new FileSystemResource(resourcesByType.get(MedicineCsvFileType.NON_RENEWAL)),
            new FileSystemResource(resourcesByType.get(MedicineCsvFileType.WITHDRAWAL)));
    return new MedicineRelease(period, inputDirectory.relativize(directory), resources);
  }

  private YearMonth parsePeriod(Path directory) {
    try {
      return YearMonth.parse(directory.getFileName().toString(), RELEASE_FORMAT);
    } catch (DateTimeParseException exception) {
      throw invalid(directory, "release directory must use YYYY-MM", exception);
    }
  }

  private Map<MedicineCsvFileType, Path> discoverCsvFiles(Path directory) throws IOException {
    var filesByType = new EnumMap<MedicineCsvFileType, Path>(MedicineCsvFileType.class);
    try (var children = Files.list(directory)) {
      for (var path : children.toList()) {
        var filename = path.getFileName().toString();
        if (!Files.isRegularFile(path)
            || !filename.toLowerCase(Locale.ROOT).endsWith(".csv")) {
          throw invalid(path, "release entries must be CSV files");
        }
        var type = MedicineCsvFileType.fromFilename(filename);
        var duplicate = filesByType.put(type, path);
        if (duplicate != null) {
          throw invalid(path, "duplicate " + type + " files: " + duplicate + " and " + path);
        }
      }
    }
    return Map.copyOf(filesByType);
  }

  private IllegalArgumentException invalid(Path path, String reason) {
    return new IllegalArgumentException("Invalid medicine release " + path + ": " + reason);
  }

  private IllegalArgumentException invalid(Path path, String reason, Exception cause) {
    return new IllegalArgumentException(
        "Invalid medicine release " + path + ": " + reason, cause);
  }
}
