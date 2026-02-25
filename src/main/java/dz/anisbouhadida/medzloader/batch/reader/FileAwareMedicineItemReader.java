package dz.anisbouhadida.medzloader.batch.reader;

import dz.anisbouhadida.medzloader.batch.dto.MedicineLine;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.item.ItemStreamException;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.ResourceAwareItemReaderItemStream;
import org.springframework.core.io.Resource;

import java.util.Objects;

/// A resource-aware [ItemReader][org.springframework.batch.infrastructure.item.ItemReader]
/// that dynamically selects the correct [FlatFileItemReader] delegate based on the
/// current resource's filename.
///
/// Each time a new resource is [opened][#open(ExecutionContext)], the
/// [MedicineItemReaderClassifier] inspects the filename and provides a reader
/// configured with the matching column layout and target DTO type.
///
/// This class implements [ResourceAwareItemReaderItemStream] so it can be used
/// with a [MultiResourceItemReader][org.springframework.batch.infrastructure.item.file.MultiResourceItemReader].
@RequiredArgsConstructor
public class FileAwareMedicineItemReader implements ResourceAwareItemReaderItemStream<MedicineLine> {

    /// The resource currently being read.
    private Resource currentResource;

    /// The delegate reader resolved for the [#currentResource].
    private FlatFileItemReader<? extends MedicineLine> delegate;

    /// Classifier used to resolve the appropriate reader for a given filename.
    private final MedicineItemReaderClassifier classifier;

    /// Stores the resource that will be read next.
    ///
    /// Called by the framework before [#open(ExecutionContext)].
    ///
    /// @param resource the CSV resource to read
    @Override
    public void setResource(@NonNull Resource resource) {
        this.currentResource = resource;
    }

    /// Reads the next [MedicineLine] from the delegate reader.
    ///
    /// @return the next line, or `null` when the resource is exhausted
    /// @throws Exception if an error occurs while reading
    @Override
    public @Nullable MedicineLine read() throws Exception {
        return delegate != null ? delegate.read() : null;
    }

    /// Initializes the delegate reader for the [#currentResource].
    ///
    /// The resource filename is passed to the [MedicineItemReaderClassifier]
    /// which returns the correctly configured [FlatFileItemReader]. The
    /// delegate is then bound to the resource and opened.
    ///
    /// @param executionContext the current step execution context
    /// @throws ItemStreamException if the delegate cannot be opened
    @Override
    public void open(@NonNull ExecutionContext executionContext) throws ItemStreamException {
        this.delegate = classifier.classify(Objects.requireNonNull(currentResource.getFilename()));
        this.delegate.setResource(currentResource);
        this.delegate.open(executionContext);
    }

    /// Propagates the execution-context update to the delegate reader, if present.
    ///
    /// @param executionContext the current step execution context
    /// @throws ItemStreamException if the delegate update fails
    @Override
    public void update(@NonNull ExecutionContext executionContext) throws ItemStreamException {
        if (delegate != null) delegate.update(executionContext);
    }

    /// Closes the delegate reader, releasing any held resources.
    ///
    /// @throws ItemStreamException if the delegate cannot be closed
    @Override
    public void close() throws ItemStreamException {
        if (delegate != null) delegate.close();
    }

}
