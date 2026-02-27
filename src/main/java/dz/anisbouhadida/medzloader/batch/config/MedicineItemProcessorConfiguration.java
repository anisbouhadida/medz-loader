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

@Configuration
public class MedicineItemProcessorConfiguration {

    @Bean
    public CompositeItemProcessor<MedicineLine, MedicineEvent> medicineItemProcessor() {
        CompositeItemProcessor<MedicineLine, MedicineEvent> processor = new CompositeItemProcessor<>();
        processor.setDelegates(List.of(
                beanValidatingItemProcessor(),
                medicineCompositeItemProcessor()
        ));
        return processor;
    }

    @Bean
    protected BeanValidatingItemProcessor<MedicineLine> beanValidatingItemProcessor() {
        BeanValidatingItemProcessor<MedicineLine>  processor = new BeanValidatingItemProcessor<>();
        processor.setFilter(true);
        return processor;
    }


    @Bean
    protected ClassifierCompositeItemProcessor<MedicineLine, MedicineEvent> medicineCompositeItemProcessor() {
        ClassifierCompositeItemProcessor<MedicineLine, MedicineEvent> processor = new ClassifierCompositeItemProcessor<>();
        processor.setClassifier(medicineLineClassifier());
        return processor;
    }

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

    @Bean
    protected ItemProcessor<NomenclatureLine, NomenclatureEvent> nomenclatureItemProcessor() {
        return medicineLineMapper()::toMedicineEvent;
    }

    @Bean
    protected ItemProcessor<WithdrawalLine, MedicineEvent> withdrawalItemProcessor() {
        return medicineLineMapper()::toMedicineEvent;
    }

    @Bean
    protected ItemProcessor<NonRenewalLine, MedicineEvent> nonRenewalItemProcessor() {
        return medicineLineMapper()::toMedicineEvent;
    }

    @Bean
    protected MedicineLineMapper medicineLineMapper() {
        return new MedicineLineMapperImpl();
    }
}
