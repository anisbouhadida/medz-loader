package dz.anisbouhadida.medzloader.batch.config.processor;

import dz.anisbouhadida.medzloader.batch.dto.MedicineLine;
import dz.anisbouhadida.medzloader.batch.dto.NomenclatureLine;
import dz.anisbouhadida.medzloader.batch.dto.NonRenewalLine;
import dz.anisbouhadida.medzloader.batch.dto.WithdrawalLine;
import dz.anisbouhadida.medzloader.batch.processor.MedicineValidationItemProcessor;
import dz.anisbouhadida.medzloader.batch.support.audit.MedicineImportIssueRecorder;
import dz.anisbouhadida.medzloader.batch.support.mapper.MedicineLineMapper;
import dz.anisbouhadida.medzloader.domain.api.MedicineApi;
import dz.anisbouhadida.medzloader.domain.model.MedicineEvent;
import dz.anisbouhadida.medzloader.domain.model.NomenclatureEvent;
import jakarta.validation.Validator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.mapstruct.factory.Mappers;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.support.ClassifierCompositeItemProcessor;
import org.springframework.batch.infrastructure.item.support.CompositeItemProcessor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.classify.Classifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/// Spring Batch configuration for the item processors that handle medicine file lines.
///
/// This configuration composes several processors:
/// - a bean validator that filters invalid lines,
/// - a classifier composite processor that delegates transformation to specific processors
///   depending on the concrete type of `MedicineLine`.
///
/// @author Anis Bouhadida
/// @since 0.0.1
/// @version 0.2.0
@Configuration
@RequiredArgsConstructor
public class MedicineItemProcessorConfiguration {

  private final MedicineApi medicineApi;

  /// Creates a [CompositeItemProcessor] that first applies bean validation,
  /// then delegates the transformation to a [ClassifierCompositeItemProcessor]
  /// which selects an appropriate processor according to the concrete line type.
  ///
  /// @return a configured [CompositeItemProcessor] ready to be injected into a Step.
  @Bean
  public CompositeItemProcessor<MedicineLine, MedicineEvent> medicineItemProcessor(
      @Qualifier("medicineValidationItemProcessor")
          ItemProcessor<MedicineLine, MedicineLine> medicineValidationItemProcessor,
      ClassifierCompositeItemProcessor<MedicineLine, MedicineEvent>
          medicineCompositeItemProcessor) {
    CompositeItemProcessor<MedicineLine, MedicineEvent> processor = new CompositeItemProcessor<>();
    processor.setDelegates(
        List.of(medicineValidationItemProcessor, medicineCompositeItemProcessor));
    return processor;
  }

  /// Provides a step-scoped validation processor that filters invalid lines and records import
  /// issues in the current job execution.
  ///
  /// @param validator Jakarta Bean Validation entry point
  /// @param issueRecorder recorder for filtered and degraded item details
  /// @param release release directory supplied as a job parameter
  /// @param jobExecutionId current Spring Batch job execution identifier
  /// @return an item processor that filters invalid items and audits validation outcomes
  @Bean
  @StepScope
  protected ItemProcessor<MedicineLine, MedicineLine> medicineValidationItemProcessor(
      Validator validator,
      MedicineImportIssueRecorder issueRecorder,
      @Value("#{jobParameters['release']}") String release,
      @Value("#{stepExecution.jobExecution.id}") Long jobExecutionId) {
    return new MedicineValidationItemProcessor(validator, issueRecorder, release, jobExecutionId);
  }

  /// Provides a [ClassifierCompositeItemProcessor] that routes each [MedicineLine]
  /// to a concrete [ItemProcessor] (nomenclature, withdrawal, non-renewal).
  ///
  /// @return a configured [ClassifierCompositeItemProcessor] for routing.
  @Bean
  protected ClassifierCompositeItemProcessor<MedicineLine, MedicineEvent>
      medicineCompositeItemProcessor() {
    ClassifierCompositeItemProcessor<MedicineLine, MedicineEvent> processor =
        new ClassifierCompositeItemProcessor<>();
    processor.setClassifier(medicineLineClassifier());
    return processor;
  }

  /// Classifier that chooses the appropriate [ItemProcessor] depending on the concrete
  /// subtype of [MedicineLine] being processed.
  ///
  /// Covered implementations: [NomenclatureLine], [WithdrawalLine], [NonRenewalLine].
  ///
  /// @return a [Classifier] that returns a suitable [ItemProcessor].
  @Bean
  protected Classifier<MedicineLine, ItemProcessor<?, ? extends MedicineEvent>>
      medicineLineClassifier() {
    return item -> {
      switch (item) {
        case NomenclatureLine _ -> {
          return nomenclatureItemProcessor();
        }
        case WithdrawalLine _ -> {
          return withdrawalItemProcessor();
        }
        case NonRenewalLine _ -> {
          return nonRenewalItemProcessor();
        }
      }
    };
  }

  /// Processor that transforms a [NomenclatureLine] into a [NomenclatureEvent].
  ///
  /// @return an [ItemProcessor] converting [NomenclatureLine] to [NomenclatureEvent].
  @Bean
  protected ItemProcessor<NomenclatureLine, NomenclatureEvent> nomenclatureItemProcessor() {
    return line -> medicineLineMapper().toMedicineEvent(line, medicineApi);
  }

  /// Processor that transforms a [WithdrawalLine] into a [MedicineEvent].
  ///
  /// @return an [ItemProcessor] converting [WithdrawalLine] to [MedicineEvent].
  @Bean
  protected ItemProcessor<WithdrawalLine, MedicineEvent> withdrawalItemProcessor() {
    return line -> medicineLineMapper().toMedicineEvent(line, medicineApi);
  }

  /// Processor that transforms a [NonRenewalLine] into a [MedicineEvent].
  ///
  /// @return an [ItemProcessor] converting [NonRenewalLine] to [MedicineEvent].
  @Bean
  protected ItemProcessor<NonRenewalLine, MedicineEvent> nonRenewalItemProcessor() {
    return line -> medicineLineMapper().toMedicineEvent(line, medicineApi);
  }

  /// Provides the mapper implementation that converts lines to domain events.
  ///
  /// @return an instance of [MedicineLineMapper].
  @Bean
  protected MedicineLineMapper medicineLineMapper() {
    return Mappers.getMapper(MedicineLineMapper.class);
  }
}
