package dz.anisbouhadida.medzloader.batch.support.classifier;

import java.util.Arrays;

/// Supported medicine CSV file types and their filename contract.
public enum MedicineCsvFileType {
  NOMENCLATURE("nomenclature"),
  WITHDRAWAL("retraits"),
  NON_RENEWAL("non_renouveles");

  private final String filenameToken;

  MedicineCsvFileType(String filenameToken) {
    this.filenameToken = filenameToken;
  }

  /// Resolves the supported type encoded in a CSV filename.
  ///
  /// @param filename filename to classify
  /// @return the recognized medicine CSV type
  /// @throws IllegalArgumentException when the filename contains no supported token
  public static MedicineCsvFileType fromFilename(String filename) {
    return Arrays.stream(values())
        .filter(type -> filename.contains(type.filenameToken))
        .findFirst()
        .orElseThrow(
            () -> new IllegalArgumentException("Unknown medicine CSV file type: " + filename));
  }
}
