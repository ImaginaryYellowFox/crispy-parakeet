package academy.incidentradar.mandatory.mr2;

import static academy.incidentradar.mandatory.mr2.TestData.BAD_LINE;
import static academy.incidentradar.mandatory.mr2.TestData.EXPECTED_RECORD;
import static academy.incidentradar.mandatory.mr2.TestData.POST_LINE;
import static academy.incidentradar.mandatory.mr2.TestData.VALID_LINE;
import static org.assertj.core.api.Assertions.assertThat;

import academy.incidentradar.filter.DateFilter;
import academy.incidentradar.model.LogRecord;
import academy.incidentradar.parse.LogLineParser;
import academy.incidentradar.processing.LogProcessor;
import academy.incidentradar.processing.ProcessingResult;
import academy.incidentradar.stats.Statistics;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("MR2. Парсер строки лога")
class LogParsingTest {

    private final LogLineParser parser = new LogLineParser();

    @Test
    @DisplayName("Эталонная строка разбирается на корректные поля")
    void referenceLineIsParsedCorrectly() {
        LogRecord actualRecord = parser.parse(VALID_LINE);
        assertThat(actualRecord).isEqualTo(EXPECTED_RECORD);
    }

    @Test
    @DisplayName("Метод, ресурс и протокол разбираются в три отдельных поля")
    void requestIsSplitIntoThreeFields() {
        LogRecord record = parser.parse(POST_LINE);

        assertThat(record.method()).as("Метод").isEqualTo("POST");
        assertThat(record.resource()).as("Ресурс").isEqualTo("/api/v1/users");
        assertThat(record.protocol()).as("Протокол").isEqualTo("HTTP/2.0");
    }

    @Test
    @DisplayName("Битая строка пропускается с warning'ом, программа не падает")
    void malformedLineIsSkippedWithWarning() {
        Stream<String> rawStream = Stream.of(VALID_LINE, BAD_LINE, VALID_LINE);
        LogProcessor processor = new LogProcessor();
        DateFilter noFilter = new DateFilter(null, null);

        ProcessingResult result = processor.process(rawStream, noFilter, List.of());
        Statistics stats = result.drain();

        assertThat(stats.totalRequests())
                .as("Должно быть обработано только 2 валидные строки (битая пропущена)")
                .isEqualTo(2);

        assertThat(stats.topResources(10))
                .as("Битая строка не должна попасть в топ ресурсов")
                .containsKey("/downloads/product_1")
                .doesNotContainKey(BAD_LINE);
    }
}
