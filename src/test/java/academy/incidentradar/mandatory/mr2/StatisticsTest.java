package academy.incidentradar.mandatory.mr2;

import static academy.incidentradar.mandatory.mr2.TestData.addResources;
import static academy.incidentradar.mandatory.mr2.TestData.addSizes;
import static academy.incidentradar.mandatory.mr2.TestData.addStatusCodes;
import static org.assertj.core.api.Assertions.assertThat;

import academy.incidentradar.stats.Statistics;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("MR2. Статистика")
class StatisticsTest {

    @Test
    @DisplayName("Пустая статистика возвращает нули")
    void emptyStatisticsReturnsZeros() {
        Statistics stats = new Statistics();

        assertThat(stats.totalRequests()).isZero();
        assertThat(stats.averageBodyBytes()).isZero();
        assertThat(stats.maxBodyBytes()).isZero();
        assertThat(stats.p95BodyBytes()).isZero();
        assertThat(stats.statusDistribution()).isEmpty();
    }

    @Test
    @DisplayName("Распределение кодов: 200,200,404,500,500,500 -> 200:2, 404:1, 500:3")
    void responseCodesAreCounted() {
        Statistics stats = new Statistics();

        addStatusCodes(stats, 200, 200, 404, 500, 500, 500);

        Map<Integer, Long> dist = stats.statusDistribution();

        assertThat(dist).hasSize(3);
        assertThat(dist.get(200)).isEqualTo(2L);
        assertThat(dist.get(404)).isEqualTo(1L);
        assertThat(dist.get(500)).isEqualTo(3L);
    }

    @Test
    @DisplayName("Top-10 ресурсов отсортирован по убыванию количества")
    void topResourcesAreSortedDescending() {
        Statistics stats = new Statistics();

        addResources(stats, "/a", "/a", "/a", "/b", "/b", "/c");

        Map<String, Long> top = stats.topResources(10);

        assertThat(top).hasSize(3);
        assertTopResource(top, 0, "/a", 3L);
        assertTopResource(top, 1, "/b", 2L);
        assertTopResource(top, 2, "/c", 1L);
    }

    @Test
    @DisplayName("Размеры 100..500: average 300, max 500, p95 500")
    void responseSizeStatistics() {
        Statistics stats = new Statistics();

        addSizes(stats, 100, 200, 300, 400, 500);

        assertThat(stats.averageBodyBytes()).isEqualTo(300.0);
        assertThat(stats.maxBodyBytes()).isEqualTo(500L);
        assertThat(stats.p95BodyBytes()).isEqualTo(500L);
    }

    @Test
    @DisplayName("Дробные значения округляются до 2 знаков после запятой")
    void fractionalValuesAreRoundedToTwoDecimals() {
        Statistics stats = new Statistics();

        addSizes(stats, 10, 20, 31);

        String formatted = String.format(Locale.ROOT, "%.2f", stats.averageBodyBytes());
        assertThat(formatted).isEqualTo("20.33");
    }

    @Test
    @DisplayName("Выбранный метод расчёта p95 зафиксирован и воспроизводим")
    void p95MethodIsDocumentedAndStable() {
        Statistics stats = new Statistics();

        for (long size = 100; size <= 500; size++) {
            stats.accept(TestData.createRecord(200, "/", size));
        }

        assertThat(stats.p95BodyBytes()).isEqualTo(480L);
    }

    private static void assertTopResource(Map<String, Long> top, int index, String expectedKey, long expectedValue) {
        assertThat(top).hasSizeGreaterThan(index);
        var entry = top.entrySet().stream().toList().get(index);

        assertThat(entry.getKey()).as("Ресурс на позиции %d", index).isEqualTo(expectedKey);
        assertThat(entry.getValue())
                .as("Количество для ресурса %s", expectedKey)
                .isEqualTo(expectedValue);
    }
}
