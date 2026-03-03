package dz.anisbouhadida.medzloader.batch.config;

import static dz.anisbouhadida.medzloader.batch.support.utils.MedicineSqlUtils.compositeKeyParams;
import static dz.anisbouhadida.medzloader.batch.support.utils.MedicineSqlUtils.loadSql;

import dz.anisbouhadida.medzloader.config.MedzLoaderProperties;
import dz.anisbouhadida.medzloader.domain.model.MedicineEvent;
import dz.anisbouhadida.medzloader.domain.model.NomenclatureEvent;
import dz.anisbouhadida.medzloader.domain.model.NonRenewalEvent;
import dz.anisbouhadida.medzloader.domain.model.WithdrawalEvent;
import java.sql.Timestamp;
import java.util.List;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.database.JdbcBatchItemWriter;
import org.springframework.batch.infrastructure.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.infrastructure.item.support.ClassifierCompositeItemWriter;
import org.springframework.batch.infrastructure.item.support.CompositeItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/// Spring Batch writer configuration for [MedicineEvent] items.
///
/// Defines all [ItemWriter][org.springframework.batch.infrastructure.item.ItemWriter] beans used
/// to persist medicine events and their associated data into the database.
/// The writers are composed into a [CompositeItemWriter] that chains:
///
/// 1. upsert of the `medicine` table,
/// 2. insertion into `medicine_status_history`,
/// 3. routing to the appropriate event-specific writer via a classifier,
/// 4. insertion into `medicine_event_history`.
///
/// @author Anis Bouhadida
/// @since 0.0.1
/// @version 0.2.0
@Configuration
@RequiredArgsConstructor
public class MedicineItemWriterConfiguration {

  private final MedzLoaderProperties properties;

  /// Creates the top-level [CompositeItemWriter] that sequentially delegates to all
  /// medicine-related writers.
  ///
  /// The execution order is:
  ///
  /// 1. `medicineItemWriter` — upserts the medicine row,
  /// 2. `statusItemWriter` — records the current status in history,
  /// 3. `classifierCompositeItemWriter` — routes to the event-type-specific writer,
  /// 4. `eventItemWriter` — appends an entry to the generic event history.
  ///
  /// @param medicineItemWriter            writer that upserts the `medicine` table
  /// @param statusItemWriter              writer that inserts into `medicine_status_history`
  /// @param classifierCompositeItemWriter classifier that routes events to typed sub-writers
  /// @param eventItemWriter               writer that inserts into `medicine_event_history`
  /// @return a configured [CompositeItemWriter] for [MedicineEvent]
  @Bean
  public CompositeItemWriter<MedicineEvent> medicineCompositeItemWriter(
      JdbcBatchItemWriter<MedicineEvent> medicineItemWriter,
      JdbcBatchItemWriter<MedicineEvent> statusItemWriter,
      ClassifierCompositeItemWriter<MedicineEvent> classifierCompositeItemWriter,
      JdbcBatchItemWriter<MedicineEvent> eventItemWriter) {
    var writer = new CompositeItemWriter<MedicineEvent>();
    writer.setDelegates(
        List.of(
            medicineItemWriter, statusItemWriter, classifierCompositeItemWriter, eventItemWriter));
    return writer;
  }

  /// Creates a [JdbcBatchItemWriter] that upserts rows in the `medicine` table.
  ///
  /// Uses an `INSERT ... ON CONFLICT DO UPDATE` statement to either insert a new
  /// medicine record or update an existing one when the composite key
  /// (`registration_number`, `code`, `icd`, `brand_name`, `laboratory_holder`) already exists.
  /// The update is only applied when the stored `version` matches the incoming value
  /// (optimistic locking).
  ///
  /// @param dataSource the JDBC [DataSource] to write to
  /// @return a configured [JdbcBatchItemWriter] for [MedicineEvent]
  @Bean
  protected JdbcBatchItemWriter<MedicineEvent> medicineItemWriter(DataSource dataSource) {

    return new JdbcBatchItemWriterBuilder<MedicineEvent>()
        .dataSource(dataSource)
        .sql(loadSql("sql/write/medicine-upsert.sql"))
        .itemSqlParameterSourceProvider(
            event ->
                compositeKeyParams(event)
                    .addValue(
                        "internationalCommonDenomination",
                        event.medicine().internationalCommonDenomination())
                    .addValue("form", event.medicine().form())
                    .addValue("dosage", event.medicine().dosage())
                    .addValue("packaging", event.medicine().packaging())
                    .addValue("list", event.medicine().list())
                    .addValue("p1", event.medicine().p1())
                    .addValue("p2", event.medicine().p2())
                    .addValue("laboratoryCountry", event.medicine().laboratoryCountry())
                    .addValue(
                        "initialRegistrationDate",
                        event.medicine().initialRegistrationDate() == null
                            ? null
                            : Timestamp.from(
                                event
                                    .medicine()
                                    .initialRegistrationDate()
                                    .atZone(properties.registrationZoneId())
                                    .toInstant()))
                    .addValue(
                        "type",
                        event.medicine().type() == null ? null : event.medicine().type().name())
                    .addValue(
                        "origin",
                        event.medicine().origin() == null ? null : event.medicine().origin().name())
                    .addValue("version", event.medicine().version()))
        .assertUpdates(false)
        .build();
  }

  /// Creates a [JdbcBatchItemWriter] that records the current medicine status
  /// in the `medicine_status_history` table.
  ///
  /// The medicine is resolved by its composite key. If the same `(medicine_id, status)`
  /// pair already exists, the insert is silently ignored (`ON CONFLICT DO NOTHING`),
  /// so each distinct status transition is recorded only once.
  ///
  /// @param dataSource the JDBC [DataSource] to write to
  /// @return a configured [JdbcBatchItemWriter] for [MedicineEvent]
  @Bean
  protected JdbcBatchItemWriter<MedicineEvent> statusItemWriter(DataSource dataSource) {
    return new JdbcBatchItemWriterBuilder<MedicineEvent>()
        .dataSource(dataSource)
        .sql(loadSql("sql/write/status-history-insert.sql"))
        .itemSqlParameterSourceProvider(
            event -> compositeKeyParams(event).addValue("status", event.status().name()))
        .assertUpdates(false)
        .build();
  }

  /// Creates a [JdbcBatchItemWriter] that records each [MedicineEvent] in the
  /// `medicine_event_history` table.
  ///
  /// The medicine is resolved by its composite key. On conflict on
  /// `(medicine_id, event_type)`, the existing row is updated with the new `event_date`.
  ///
  /// @param dataSource the JDBC [DataSource] to write to
  /// @return a configured [JdbcBatchItemWriter] for [MedicineEvent]
  @Bean
  protected JdbcBatchItemWriter<MedicineEvent> eventItemWriter(DataSource dataSource) {
    return new JdbcBatchItemWriterBuilder<MedicineEvent>()
        .dataSource(dataSource)
        .sql(loadSql("sql/write/event-history-upsert.sql"))
        .itemSqlParameterSourceProvider(
            event -> compositeKeyParams(event).addValue("eventType", event.eventType().name()))
        .assertUpdates(false)
        .build();
  }

  /// Creates a [ClassifierCompositeItemWriter] that routes each [MedicineEvent]
  /// to the appropriate event-type-specific writer using sealed-type pattern matching.
  ///
  /// @param nomenclatureEventItemWriter writer for nomenclature (upsert) events
  /// @param nonRenewalEventItemWriter   writer for non-renewal events
  /// @param withdrawalEventItemWriter   writer for withdrawal events
  /// @return a configured [ClassifierCompositeItemWriter] for [MedicineEvent]
  @Bean
  @SuppressWarnings("unchecked")
  protected ClassifierCompositeItemWriter<MedicineEvent> classifierCompositeItemWriter(
      @Qualifier("nomenclatureEventItemWriter")
          ItemWriter<? extends MedicineEvent> nomenclatureEventItemWriter,
      @Qualifier("nonRenewalEventItemWriter")
          ItemWriter<? extends MedicineEvent> nonRenewalEventItemWriter,
      @Qualifier("withdrawalEventItemWriter")
          ItemWriter<? extends MedicineEvent> withdrawalEventItemWriter) {

    var writer = new ClassifierCompositeItemWriter<MedicineEvent>();
    writer.setClassifier(
        event ->
            switch (event) {
              case NomenclatureEvent _ ->
                  (ItemWriter<? super MedicineEvent>) nomenclatureEventItemWriter;
              case WithdrawalEvent _ ->
                  (ItemWriter<? super MedicineEvent>) withdrawalEventItemWriter;
              case NonRenewalEvent _ ->
                  (ItemWriter<? super MedicineEvent>) nonRenewalEventItemWriter;
            });
    return writer;
  }

  /// Creates an [ItemWriter] that upserts rows in the `nomenclature_event` table
  /// for [NomenclatureEvent] items.
  ///
  /// The medicine is resolved by its composite key. On conflict on `medicine_id`,
  /// the existing row is updated with the latest `final_registration_date`,
  /// `stability_duration`, and `observations` values.
  ///
  /// @param dataSource the JDBC [DataSource] to write to
  /// @return a configured [ItemWriter] for [NomenclatureEvent]
  @Bean
  protected ItemWriter<NomenclatureEvent> nomenclatureEventItemWriter(DataSource dataSource) {
    return new JdbcBatchItemWriterBuilder<NomenclatureEvent>()
        .dataSource(dataSource)
        .sql(loadSql("sql/write/nomenclature-event-upsert.sql"))
        .itemSqlParameterSourceProvider(
            event ->
                compositeKeyParams(event)
                    .addValue("finalRegistrationDate", event.finalRegistrationDate())
                    .addValue("stabilityDuration", event.stabilityDuration())
                    .addValue("observations", event.observations()))
        .build();
  }

  /// Creates an [ItemWriter] that upserts rows in the `non_renewal_event` table
  /// for [NonRenewalEvent] items.
  ///
  /// The medicine is resolved by its composite key. On conflict on `medicine_id`,
  /// the existing row is updated with the latest `final_registration_date`
  /// and `observations` values.
  ///
  /// @param dataSource the JDBC [DataSource] to write to
  /// @return a configured [ItemWriter] for [NonRenewalEvent]
  @Bean
  protected ItemWriter<NonRenewalEvent> nonRenewalEventItemWriter(DataSource dataSource) {
    return new JdbcBatchItemWriterBuilder<NonRenewalEvent>()
        .dataSource(dataSource)
        .sql(loadSql("sql/write/non-renewal-event-upsert.sql"))
        .itemSqlParameterSourceProvider(
            event ->
                compositeKeyParams(event)
                    .addValue("finalRegistrationDate", event.finalRegistrationDate())
                    .addValue("observations", event.observations()))
        .assertUpdates(false)
        .build();
  }

  /// Creates an [ItemWriter] that upserts rows in the `withdrawal_event` table
  /// for [WithdrawalEvent] items.
  ///
  /// The medicine is resolved by its composite key. On conflict on `medicine_id`,
  /// the existing row is updated with the latest `withdrawal_date`
  /// and `withdrawal_reason` values.
  ///
  /// @param dataSource the JDBC [DataSource] to write to
  /// @return a configured [ItemWriter] for [WithdrawalEvent]
  @Bean
  protected ItemWriter<WithdrawalEvent> withdrawalEventItemWriter(DataSource dataSource) {
    return new JdbcBatchItemWriterBuilder<WithdrawalEvent>()
        .dataSource(dataSource)
        .sql(loadSql("sql/write/withdrawal-event-upsert.sql"))
        .itemSqlParameterSourceProvider(
            event ->
                compositeKeyParams(event)
                    .addValue("withdrawalDate", event.withdrawalDate())
                    .addValue("withdrawalReason", event.withdrawalReason()))
        .assertUpdates(false)
        .build();
  }
}
