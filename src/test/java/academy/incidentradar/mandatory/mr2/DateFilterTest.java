package academy.incidentradar.mandatory.mr2;

import static academy.incidentradar.mandatory.mr2.TestData.DAY_1;
import static academy.incidentradar.mandatory.mr2.TestData.DAY_2;
import static academy.incidentradar.mandatory.mr2.TestData.DAY_3;
import static academy.incidentradar.mandatory.mr2.TestData.createRecord;
import static org.assertj.core.api.Assertions.assertThat;

import academy.incidentradar.filter.DateFilter;
import academy.incidentradar.model.LogRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("MR2. Фильтр по датам")
class DateFilterTest {

    private static final LogRecord REC_DAY_1 = createRecord(DAY_1);
    private static final LogRecord REC_DAY_2 = createRecord(DAY_2);
    private static final LogRecord REC_DAY_3 = createRecord(DAY_3);

    @Test
    @DisplayName("Без --from и --to обрабатываются все записи")
    void noFilterKeepsEverything() {
        DateFilter filter = new DateFilter(null, null);
        assertFilter(filter, true, true, true);
    }

    @Test
    @DisplayName("Только --from: открытая правая граница")
    void onlyFromIsOpenEnded() {
        DateFilter filter = new DateFilter(DAY_2, null);
        assertFilter(filter, false, true, true);
    }

    @Test
    @DisplayName("Только --to: открытая левая граница")
    void onlyToIsOpenEnded() {
        DateFilter filter = new DateFilter(null, DAY_2);
        assertFilter(filter, true, true, false);
    }

    @Test
    @DisplayName("Обе границы: период включительно")
    void bothBoundsAreInclusive() {
        DateFilter filter = new DateFilter(DAY_1, DAY_3);
        assertFilter(filter, true, true, true);

        assertThat(filter.test(createRecord(DAY_1.minusDays(1))))
                .as("День до периода должен отсекаться")
                .isFalse();
        assertThat(filter.test(createRecord(DAY_3.plusDays(1))))
                .as("День после периода должен отсекаться")
                .isFalse();
    }

    private static void assertFilter(DateFilter filter, boolean expectDay1, boolean expectDay2, boolean expectDay3) {
        assertThat(filter.test(REC_DAY_1)).as("День 1").isEqualTo(expectDay1);
        assertThat(filter.test(REC_DAY_2)).as("День 2").isEqualTo(expectDay2);
        assertThat(filter.test(REC_DAY_3)).as("День 3").isEqualTo(expectDay3);
    }
}
