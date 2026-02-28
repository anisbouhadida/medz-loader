package dz.anisbouhadida.medzloader.batch.config;

import dz.anisbouhadida.medzloader.batch.dto.MedicineLine;
import dz.anisbouhadida.medzloader.batch.dto.NomenclatureLine;
import dz.anisbouhadida.medzloader.batch.dto.NonRenewalLine;
import dz.anisbouhadida.medzloader.batch.dto.WithdrawalLine;
import dz.anisbouhadida.medzloader.batch.support.mapper.MedicineLineMapper;
import dz.anisbouhadida.medzloader.batch.support.mapper.MedicineLineMapperImpl;
import dz.anisbouhadida.medzloader.domain.model.MedicineEvent;
import dz.anisbouhadida.medzloader.domain.model.NomenclatureEvent;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.support.ClassifierCompositeItemProcessor;
import org.springframework.batch.infrastructure.item.support.CompositeItemProcessor;
import org.springframework.batch.infrastructure.item.validator.BeanValidatingItemProcessor;
import org.springframework.classify.Classifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/// Spring Batch configuration for the item processors that handle medicine file lines.
///
/// This configuration composes several processors:
/// - a bean validator that filters invalid lines,
/// - a classifier composite processor that delegates transformation to specific processors
///   depending on the concrete type of `MedicineLine`.
///
@Configuration
public class MedicineItemProcessorConfiguration {

    /// Creates a [CompositeItemProcessor] that first applies bean validation,
    /// then delegates the transformation to a [ClassifierCompositeItemProcessor]
    /// which selects an appropriate processor according to the concrete line type.
    ///
    /// @return a configured [CompositeItemProcessor] ready to be injected into a Step.
    @Bean
    public CompositeItemProcessor<MedicineLine, MedicineEvent> medicineItemProcessor() {
        CompositeItemProcessor<MedicineLine, MedicineEvent> processor = new CompositeItemProcessor<>();
        processor.setDelegates(List.of(
                beanValidatingItemProcessor(),
                medicineCompositeItemProcessor()
        ));
        return processor;
    }

    /// Provides a [BeanValidatingItemProcessor] configured to filter
    /// invalid lines instead of throwing exceptions.
    ///
    /// @return a [BeanValidatingItemProcessor] that filters invalid items.
    @Bean
    protected BeanValidatingItemProcessor<MedicineLine> beanValidatingItemProcessor() {
        BeanValidatingItemProcessor<MedicineLine>  processor = new BeanValidatingItemProcessor<>();
        processor.setFilter(true);
        return processor;
    }

    /// Provides a [ClassifierCompositeItemProcessor] that routes each [MedicineLine]
    /// to a concrete [ItemProcessor] (nomenclature, withdrawal, non-renewal).
    ///
    /// @return a configured [ClassifierCompositeItemProcessor] for routing.
    @Bean
    protected ClassifierCompositeItemProcessor<MedicineLine, MedicineEvent> medicineCompositeItemProcessor() {
        ClassifierCompositeItemProcessor<MedicineLine, MedicineEvent> processor = new ClassifierCompositeItemProcessor<>();
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
    protected Classifier<MedicineLine, ItemProcessor<?, ? extends MedicineEvent>> medicineLineClassifier() {
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
        return medicineLineMapper()::toMedicineEvent;
    }

    /// Processor that transforms a [WithdrawalLine] into a [MedicineEvent].
    ///
    /// @return an [ItemProcessor] converting [WithdrawalLine] to [MedicineEvent].
    @Bean
    protected ItemProcessor<WithdrawalLine, MedicineEvent> withdrawalItemProcessor() {
        return medicineLineMapper()::toMedicineEvent;
    }

    /// Processor that transforms a [NonRenewalLine] into a [MedicineEvent].
    ///
    /// @return an [ItemProcessor] converting [NonRenewalLine] to [MedicineEvent].
    @Bean
    protected ItemProcessor<NonRenewalLine, MedicineEvent> nonRenewalItemProcessor() {
        return medicineLineMapper()::toMedicineEvent;
    }

    /// Provides the mapper implementation that converts lines to domain events.
    ///
    /// @return an instance of [MedicineLineMapper].
    @Bean
    protected MedicineLineMapper medicineLineMapper() {
        return new MedicineLineMapperImpl();
    }
}
