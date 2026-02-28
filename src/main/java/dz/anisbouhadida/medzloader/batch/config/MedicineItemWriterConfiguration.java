package dz.anisbouhadida.medzloader.batch.config;

import dz.anisbouhadida.medzloader.domain.model.MedicineEvent;
import dz.anisbouhadida.medzloader.domain.model.NomenclatureEvent;
import dz.anisbouhadida.medzloader.domain.model.NonRenewalEvent;
import dz.anisbouhadida.medzloader.domain.model.WithdrawalEvent;
import dz.anisbouhadida.medzloader.domain.model.enums.MedicineEventType;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.database.ItemSqlParameterSourceProvider;
import org.springframework.batch.infrastructure.item.database.JdbcBatchItemWriter;
import org.springframework.batch.infrastructure.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.infrastructure.item.support.ClassifierCompositeItemWriter;
import org.springframework.batch.infrastructure.item.support.CompositeItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.util.List;

@Configuration
public class MedicineItemWriterConfiguration {

    ItemSqlParameterSourceProvider<MedicineEvent> params = m -> {
        var p = new MapSqlParameterSource();

        p.addValue("registrationNumber", m.medicine().registrationNumber());
        p.addValue("code", m.medicine().code());
        p.addValue("internationalCommonDenomination", m.medicine().internationalCommonDenomination()); // DB column is icd
        p.addValue("brandName", m.medicine().brandName());
        p.addValue("form", m.medicine().form());
        p.addValue("dosage", m.medicine().dosage());
        p.addValue("packaging", m.medicine().packaging());
        p.addValue("list", m.medicine().list());
        p.addValue("p1", m.medicine().p1());
        p.addValue("p2", m.medicine().p2());
        p.addValue("laboratoryHolder", m.medicine().laboratoryHolder());
        p.addValue("laboratoryCountry", m.medicine().laboratoryCountry());

        // timestamptz: choose how to interpret LocalDateTime (here: Europe/Paris)
        p.addValue("initialRegistrationDate",
                m.medicine().initialRegistrationDate() == null ? null
                        : Timestamp.from(m.medicine().initialRegistrationDate()
                        .atZone(ZoneId.of("Europe/Paris"))
                        .toInstant()));

        // enums will bind as strings like "GE" / "MANUFACTURED"
        p.addValue("type", m.medicine().type() == null ? null : m.medicine().type().name());
        p.addValue("origin", m.medicine().origin() == null ? null : m.medicine().origin().name());

        p.addValue("version", m.medicine().version());
        return p;
    };


    @Bean
    public CompositeItemWriter<MedicineEvent> medicineCompositeItemWriter(
            JdbcBatchItemWriter<MedicineEvent> medicineItemWriter,
            JdbcBatchItemWriter<MedicineEvent> statusItemWriter,
            ClassifierCompositeItemWriter<MedicineEvent> classifierCompositeItemWriter,
            JdbcBatchItemWriter<MedicineEvent> eventItemWriter
    ) {
        CompositeItemWriter<MedicineEvent> writer = new CompositeItemWriter<>();
        writer.setDelegates(List.of(
                medicineItemWriter,
                statusItemWriter,
                classifierCompositeItemWriter,
                eventItemWriter
        ));
        return writer;
    }

    @Bean
    protected JdbcBatchItemWriter<MedicineEvent> medicineItemWriter(DataSource dataSource) {
        var sql = """
                INSERT INTO medicine
                            (
                                        registration_number,
                                        code,
                                        icd,
                                        brand_name,
                                        form,
                                        dosage,
                                        packaging,
                                        list,
                                        p1,
                                        p2,
                                        laboratory_holder,
                                        laboratory_country,
                                        initial_registration_date,
                                        type,
                                        origin,
                                        version,
                                        last_updated
                            )
                            VALUES
                            (
                                        :registrationNumber,
                                        :code,
                                        :internationalCommonDenomination,
                                        :brandName,
                                        :form,
                                        :dosage,
                                        :packaging,
                                        :list,
                                        :p1,
                                        :p2,
                                        :laboratoryHolder,
                                        :laboratoryCountry,
                                        :initialRegistrationDate,
                                        :type::medicine_type,
                                        :origin::medicine_origin,
                                        :version,
                                        CURRENT_TIMESTAMP
                            )
                ON CONFLICT
                            (
                                        registration_number,
                                        code,
                                        icd,
                                        brand_name,
                                        laboratory_holder
                            )
                DO UPDATE SET
                       form = excluded.form,
                       dosage = excluded.dosage,
                       packaging = excluded.packaging,
                       list = excluded.list,
                       p1 = excluded.p1,
                       p2 = excluded.p2,
                       initial_registration_date = excluded.initial_registration_date,
                       type = excluded.type::medicine_type,
                       origin = excluded.origin::medicine_origin,
                       version = medicine.version + 1,
                       last_updated = CURRENT_TIMESTAMP
                WHERE  medicine.version = excluded.version;
                """;

        return new JdbcBatchItemWriterBuilder<MedicineEvent>().dataSource(dataSource)
                .sql(sql)
                .itemSqlParameterSourceProvider(params)
                .assertUpdates(false)
                .build();
    }

    @Bean
    protected JdbcBatchItemWriter<MedicineEvent> statusItemWriter(DataSource dataSource) {
        var sql = """
                INSERT INTO medicine_status_history (medicine_id, status, status_timestamp)
                VALUES (
                  (SELECT medicine_id FROM medicine
                   WHERE registration_number = :registrationNumber
                     AND code = :code
                     AND icd = :icd
                     AND brand_name = :brandName
                     AND laboratory_holder = :laboratoryHolder
                     ORDER BY last_updated DESC LIMIT 1),
                  :status::medicine_status,
                  CURRENT_TIMESTAMP
                )
                ON CONFLICT (medicine_id, status) DO NOTHING;
                """;
        ItemSqlParameterSourceProvider<MedicineEvent> provider = evt ->
                new MapSqlParameterSource()
                        .addValue("registrationNumber", evt.medicine().registrationNumber())
                        .addValue("code", evt.medicine().code())
                        .addValue("icd", evt.medicine().internationalCommonDenomination())
                        .addValue("brandName", evt.medicine().brandName())
                        .addValue("laboratoryHolder", evt.medicine().laboratoryHolder())
                        .addValue("status", evt.status().name());

        return new JdbcBatchItemWriterBuilder<MedicineEvent>()
                .dataSource(dataSource)
                .sql(sql)
                .itemSqlParameterSourceProvider(provider)
                .assertUpdates(false)
                .build();
    }

    @Bean
    protected ClassifierCompositeItemWriter<MedicineEvent> classifierCompositeItemWriter(
            @Qualifier("nomenclatureEventItemWriter") ItemWriter<? extends MedicineEvent> nomenclatureEventItemWriter,
            @Qualifier("nonRenewalEventItemWriter") ItemWriter<? extends MedicineEvent> nonRenewalEventItemWriter,
            @Qualifier("withdrawalEventItemWriter") ItemWriter<? extends MedicineEvent> withdrawalEventItemWriter) {

        ClassifierCompositeItemWriter<MedicineEvent> writer = new ClassifierCompositeItemWriter<>();
        writer.setClassifier(medicineEvent -> {
            switch (medicineEvent.eventType()) {
                case MedicineEventType.UPSERT -> {
                    return (ItemWriter<? super MedicineEvent>) nomenclatureEventItemWriter;
                }
                case MedicineEventType.NON_RENEWAL -> {
                    return (ItemWriter<? super MedicineEvent>) nonRenewalEventItemWriter;
                }
                case MedicineEventType.WITHDRAWAL -> {
                    return (ItemWriter<? super MedicineEvent>) withdrawalEventItemWriter;
                }
                default -> throw new IllegalArgumentException("Unknown event type: " + medicineEvent.eventType());
            }
        });
        return writer;
    }

    @Bean
    public ItemWriter<NomenclatureEvent> nomenclatureEventItemWriter(DataSource dataSource) {
        var sql = """
                INSERT INTO nomenclature_event (
                  medicine_id, final_registration_date, stability_duration, observations
                )
                VALUES (
                  (SELECT medicine_id FROM medicine
                   WHERE registration_number = :registrationNumber
                     AND code = :code
                     AND icd = :icd
                     AND brand_name = :brandName
                     AND laboratory_holder = :laboratoryHolder
                   ORDER BY last_updated DESC LIMIT 1),
                  :finalRegistrationDate, :stabilityDuration, :observations
                )
                ON CONFLICT (medicine_id) DO UPDATE SET
                  final_registration_date = EXCLUDED.final_registration_date,
                  stability_duration = EXCLUDED.stability_duration,
                  observations = EXCLUDED.observations,
                  updated_at = CURRENT_TIMESTAMP;
                """;

        return new JdbcBatchItemWriterBuilder<NomenclatureEvent>()
                .dataSource(dataSource)
                .sql(sql)
                .itemSqlParameterSourceProvider(e ->
                        new org.springframework.jdbc.core.namedparam.MapSqlParameterSource()
                                .addValue("registrationNumber", e.medicine().registrationNumber())
                                .addValue("code", e.medicine().code())
                                .addValue("icd", e.medicine().internationalCommonDenomination())
                                .addValue("brandName", e.medicine().brandName())
                                .addValue("laboratoryHolder", e.medicine().laboratoryHolder())
                                .addValue("finalRegistrationDate", e.finalRegistrationDate())
                                .addValue("stabilityDuration", e.stabilityDuration())
                                .addValue("observations", e.observations())
                )
                .build();
    }

    @Bean
    public ItemWriter<NonRenewalEvent> nonRenewalEventItemWriter(DataSource dataSource) {
        var sql = """
                INSERT INTO non_renewal_event (
                  medicine_id, final_registration_date, observations
                )
                VALUES (
                  (SELECT medicine_id FROM medicine
                   WHERE registration_number = :registrationNumber
                     AND code = :code
                     AND icd = :icd
                     AND brand_name = :brandName
                     AND laboratory_holder = :laboratoryHolder
                   ORDER BY last_updated DESC LIMIT 1),
                  :finalRegistrationDate, :observations
                )
                ON CONFLICT (medicine_id) DO UPDATE SET
                  final_registration_date = EXCLUDED.final_registration_date,
                  observations = EXCLUDED.observations,
                  updated_at = CURRENT_TIMESTAMP;
                """;

        return new JdbcBatchItemWriterBuilder<NonRenewalEvent>()
                .dataSource(dataSource)
                .sql(sql)
                .itemSqlParameterSourceProvider(e ->
                        new org.springframework.jdbc.core.namedparam.MapSqlParameterSource()
                                .addValue("registrationNumber", e.medicine().registrationNumber())
                                .addValue("code", e.medicine().code())
                                .addValue("icd", e.medicine().internationalCommonDenomination())
                                .addValue("brandName", e.medicine().brandName())
                                .addValue("laboratoryHolder", e.medicine().laboratoryHolder())
                                .addValue("finalRegistrationDate", e.finalRegistrationDate())
                                .addValue("observations", e.observations())
                )
                .assertUpdates(false)
                .build();
    }

    @Bean
    public ItemWriter<WithdrawalEvent> withdrawalEventItemWriter(DataSource dataSource) {
        var sql = """
                INSERT INTO withdrawal_event (
                  medicine_id, withdrawal_date, withdrawal_reason
                )
                VALUES (
                  (SELECT medicine_id FROM medicine
                   WHERE registration_number = :registrationNumber
                     AND code = :code
                     AND icd = :icd
                     AND brand_name = :brandName
                     AND laboratory_holder = :laboratoryHolder
                   ORDER BY last_updated DESC LIMIT 1),
                  :withdrawalDate, :withdrawalReason
                )
                ON CONFLICT (medicine_id) DO UPDATE SET
                  withdrawal_date = EXCLUDED.withdrawal_date,
                  withdrawal_reason = EXCLUDED.withdrawal_reason,
                  updated_at = CURRENT_TIMESTAMP;
                """;

        return new JdbcBatchItemWriterBuilder<WithdrawalEvent>()
                .dataSource(dataSource)
                .sql(sql)
                .itemSqlParameterSourceProvider(e ->
                        new org.springframework.jdbc.core.namedparam.MapSqlParameterSource()
                                .addValue("registrationNumber", e.medicine().registrationNumber())
                                .addValue("code", e.medicine().code())
                                .addValue("icd", e.medicine().internationalCommonDenomination())
                                .addValue("brandName", e.medicine().brandName())
                                .addValue("laboratoryHolder", e.medicine().laboratoryHolder())
                                .addValue("withdrawalDate", e.withdrawalDate())
                                .addValue("withdrawalReason", e.withdrawalReason())
                )
                .assertUpdates(false)
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<MedicineEvent> eventItemWriter(DataSource dataSource) {
        var sql = """
                INSERT INTO medicine_event_history (medicine_id, event_type, event_date)
                VALUES (
                  (SELECT medicine_id FROM medicine
                   WHERE registration_number = :registrationNumber
                     AND code = :code
                     AND icd = :icd
                     AND brand_name = :brandName
                     AND laboratory_holder = :laboratoryHolder
                   ORDER BY last_updated DESC LIMIT 1),
                  :eventType::medicine_event_type,
                  CURRENT_TIMESTAMP
                )
                ON CONFLICT (medicine_id, event_type) DO UPDATE SET
                  event_date = EXCLUDED.event_date,
                  updated_at = CURRENT_TIMESTAMP;
                """;

        return new JdbcBatchItemWriterBuilder<MedicineEvent>()
                .dataSource(dataSource)
                .sql(sql)
                .itemSqlParameterSourceProvider(e ->
                        new org.springframework.jdbc.core.namedparam.MapSqlParameterSource()
                                .addValue("registrationNumber", e.medicine().registrationNumber())
                                .addValue("code", e.medicine().code())
                                .addValue("icd", e.medicine().internationalCommonDenomination())
                                .addValue("brandName", e.medicine().brandName())
                                .addValue("laboratoryHolder", e.medicine().laboratoryHolder())
                                .addValue("eventType", e.eventType().name())
                )
                .assertUpdates(false)
                .build();
    }


}
