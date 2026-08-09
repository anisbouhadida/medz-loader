package dz.anisbouhadida.medzloader.batch.support.audit;

/// Describes how a medicine import issue affected the source item.
public enum MedicineImportIssueDisposition {
  /// The item failed validation and was filtered from the batch output.
  FILTERED,
  /// The item was loaded, but an optional source value was missing or degraded.
  LOADED_WITH_WARNING
}
