package dz.anisbouhadida.medzloader.batch.reader;

import dz.anisbouhadida.medzloader.batch.dto.MedicineLine;
import dz.anisbouhadida.medzloader.batch.dto.NomenclatureLine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.core.io.Resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("FileAwareMedicineItemReader")
@ExtendWith(MockitoExtension.class)
class FileAwareMedicineItemReaderTest {

    @Mock
    private MedicineItemReaderClassifier classifier;

    @Mock
    private Resource resource;

    @Mock
    private FlatFileItemReader<MedicineLine> delegateReader;

    @Mock
    private ExecutionContext executionContext;

    private FileAwareMedicineItemReader reader;

    @BeforeEach
    void setUp() {
        reader = new FileAwareMedicineItemReader(classifier);
    }

    @Nested
    @DisplayName("open()")
    class OpenTests {

        @Test
        @DisplayName("should classify the resource and open the delegate")
        void open_should_classifyAndOpenDelegate_when_resourceIsSet() {
            when(resource.getFilename()).thenReturn("nomenclature.csv");
            doReturn(delegateReader).when(classifier).classify("nomenclature.csv");
            reader.setResource(resource);

            reader.open(executionContext);

            verify(classifier).classify("nomenclature.csv");
            verify(delegateReader).setResource(resource);
            verify(delegateReader).open(executionContext);
        }
    }

    @Nested
    @DisplayName("read()")
    class ReadTests {

        @Test
        @DisplayName("should return null when delegate is not set")
        void read_should_returnNull_when_delegateNotInitialized() throws Exception {
            assertThat(reader.read()).isNull();
        }

        @Test
        @DisplayName("should delegate read to the resolved reader")
        void read_should_delegateToResolvedReader_when_opened() throws Exception {
            NomenclatureLine expectedLine = mock(NomenclatureLine.class);
            when(resource.getFilename()).thenReturn("nomenclature.csv");
            doReturn(delegateReader).when(classifier).classify("nomenclature.csv");
            when(delegateReader.read()).thenReturn(expectedLine);

            reader.setResource(resource);
            reader.open(executionContext);

            MedicineLine result = reader.read();

            assertThat(result).isSameAs(expectedLine);
        }

        @Test
        @DisplayName("should return null when delegate reader is exhausted")
        void read_should_returnNull_when_delegateReaderIsExhausted() throws Exception {
            when(resource.getFilename()).thenReturn("nomenclature.csv");
            doReturn(delegateReader).when(classifier).classify("nomenclature.csv");
            when(delegateReader.read()).thenReturn(null);

            reader.setResource(resource);
            reader.open(executionContext);

            assertThat(reader.read()).isNull();
        }
    }

    @Nested
    @DisplayName("update()")
    class UpdateTests {

        @Test
        @DisplayName("should do nothing when delegate is not set")
        void update_should_doNothing_when_delegateNotInitialized() {
            reader.update(executionContext);

            verifyNoInteractions(classifier);
        }

        @Test
        @DisplayName("should propagate update to delegate")
        void update_should_propagateToDelegate_when_opened() {
            when(resource.getFilename()).thenReturn("nomenclature.csv");
            doReturn(delegateReader).when(classifier).classify("nomenclature.csv");

            reader.setResource(resource);
            reader.open(executionContext);
            reader.update(executionContext);

            verify(delegateReader).update(executionContext);
        }
    }

    @Nested
    @DisplayName("close()")
    class CloseTests {

        @Test
        @DisplayName("should do nothing when delegate is not set")
        void close_should_doNothing_when_delegateNotInitialized() {
            reader.close();

            verifyNoInteractions(classifier);
        }

        @Test
        @DisplayName("should close the delegate reader")
        void close_should_closeDelegate_when_opened() {
            when(resource.getFilename()).thenReturn("nomenclature.csv");
            doReturn(delegateReader).when(classifier).classify("nomenclature.csv");

            reader.setResource(resource);
            reader.open(executionContext);
            reader.close();

            verify(delegateReader).close();
        }
    }
}
