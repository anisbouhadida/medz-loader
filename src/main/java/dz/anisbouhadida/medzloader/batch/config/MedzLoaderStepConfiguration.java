package dz.anisbouhadida.medzloader.batch.config;

import dz.anisbouhadida.medzloader.batch.dto.MedicineLine;
import dz.anisbouhadida.medzloader.domain.model.Medicine;
import dz.anisbouhadida.medzloader.domain.model.MedicineEvent;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/// Configuration class that defines the main batch [Step] for loading medicine data.
///
/// The step reads [MedicineLine] records from CSV files, processes them into
/// [Medicine] domain objects, and writes the results using the provided
/// [ItemWriter].
@Configuration
public class MedzLoaderStepConfiguration {

    /// Builds the chunk-oriented [Step] that orchestrates the medicine loading pipeline.
    ///
    /// Each chunk processes **10** items at a time within a single transaction.
    ///
    /// @param jobRepository       the repository used to persist step metadata
    /// @param transactionManager  the transaction manager governing chunk boundaries
    /// @param multiCsvItemReader          the reader supplying [MedicineLine] records
    /// @param itemWriter          the writer persisting [Medicine] objects
    /// @return a fully configured batch step
    @Bean
    public Step medzLoaderStep(JobRepository jobRepository,
                               PlatformTransactionManager transactionManager,
                               ItemReader<MedicineLine> multiCsvItemReader,
                               ItemProcessor<MedicineLine, MedicineEvent> medicineItemProcessor,
                               ItemWriter<MedicineEvent> itemWriter) {
        return new StepBuilder("medzLoaderStep",jobRepository)
                .<MedicineLine,MedicineEvent>chunk(10).transactionManager(transactionManager)
                .reader(multiCsvItemReader)
                .processor(medicineItemProcessor)
                .writer(itemWriter)
                .build();

    }
}
