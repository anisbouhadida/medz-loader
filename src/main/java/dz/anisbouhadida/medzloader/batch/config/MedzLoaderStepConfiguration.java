package dz.anisbouhadida.medzloader.batch.config;

import dz.anisbouhadida.medzloader.batch.dto.MedicineLine;
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
/// [MedicineEvent] domain objects, and writes the results using the provided
/// [ItemWriter].
///
/// @author Anis Bouhadida
/// @since 0.0.1
@Configuration
public class MedzLoaderStepConfiguration {

    /// Builds the chunk-oriented [Step] that orchestrates the medicine loading pipeline.
    ///
    /// Each chunk processes **100** items at a time within a single transaction.
    ///
    /// @param jobRepository       the repository used to persist step metadata
    /// @param transactionManager  the transaction manager governing chunk boundaries
    /// @param multiCsvItemReader          the reader supplying [MedicineLine] records
    /// @param medicineItemProcessor       the processor transforming [MedicineLine] to [MedicineEvent]
    /// @param medicineCompositeItemWriter          the writer persisting [MedicineEvent] objects
    /// @return a fully configured batch step
    @Bean
    public Step medzLoaderStep(JobRepository jobRepository,
                               PlatformTransactionManager transactionManager,
                               ItemReader<MedicineLine> multiCsvItemReader,
                               ItemProcessor<MedicineLine, MedicineEvent> medicineItemProcessor,
                               ItemWriter<MedicineEvent> medicineCompositeItemWriter) {
        return new StepBuilder("medzLoaderStep",jobRepository)
                .<MedicineLine,MedicineEvent>chunk(100).transactionManager(transactionManager)
                .reader(multiCsvItemReader)
                .processor(medicineItemProcessor)
                .writer(medicineCompositeItemWriter)
                .faultTolerant()
                .build();

    }
}
