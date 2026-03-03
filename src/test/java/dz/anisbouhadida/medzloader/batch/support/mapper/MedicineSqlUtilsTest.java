package dz.anisbouhadida.medzloader.batch.support.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import dz.anisbouhadida.medzloader.batch.support.utils.MedicineSqlUtils;
import dz.anisbouhadida.medzloader.domain.model.Medicine;
import dz.anisbouhadida.medzloader.domain.model.NomenclatureEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@DisplayName("MedicineSqlUtils")
class MedicineSqlUtilsTest {

  @Test
  @DisplayName("compositeKeyParams should map all five composite-key fields")
  void compositeKeyParams_should_mapAllFiveFields() {
    var medicine =
        new Medicine(
            "REG-001",
            "CODE-42",
            "Paracetamol",
            "Doliprane",
            "tablet",
            "500mg",
            "box of 20",
            "I",
            "100",
            "200",
            "Sanofi",
            "France",
            null,
            null,
            null,
            1);
    var event = new NomenclatureEvent(medicine, null, null, null);

    MapSqlParameterSource params = MedicineSqlUtils.compositeKeyParams(event);

    assertThat(params.getValue("registrationNumber")).isEqualTo("REG-001");
    assertThat(params.getValue("code")).isEqualTo("CODE-42");
    assertThat(params.getValue("icd")).isEqualTo("Paracetamol");
    assertThat(params.getValue("brandName")).isEqualTo("Doliprane");
    assertThat(params.getValue("laboratoryHolder")).isEqualTo("Sanofi");
  }

  @Test
  @DisplayName("compositeKeyParams should return a mutable source for chaining")
  void compositeKeyParams_should_allowChaining() {
    var medicine =
        new Medicine(
            "REG-002",
            "CODE-99",
            "Ibuprofen",
            "Advil",
            "capsule",
            "400mg",
            "box of 10",
            "II",
            "50",
            "80",
            "Pfizer",
            "USA",
            null,
            null,
            null,
            0);
    var event = new NomenclatureEvent(medicine, null, null, null);

    MapSqlParameterSource params =
        MedicineSqlUtils.compositeKeyParams(event).addValue("extra", "value");

    assertThat(params.hasValue("extra")).isTrue();
    assertThat(params.getValue("extra")).isEqualTo("value");
    assertThat(params.hasValue("registrationNumber")).isTrue();
  }
}
