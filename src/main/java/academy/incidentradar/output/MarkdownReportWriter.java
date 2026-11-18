package academy.incidentradar.output;

import academy.incidentradar.config.OutputFormat;
import academy.incidentradar.config.ProcessingConfig;
import academy.incidentradar.model.LogRecord;
import academy.incidentradar.processing.ProcessingResult;
import academy.incidentradar.stats.Statistics;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;

public final class MarkdownReportWriter implements ReportWriter {

    private static final int HEADING_LEVEL_1 = 1;
    private static final int HEADING_LEVEL_2 = 2;
    private static final int HEADING_LEVEL_3 = 3;
    private static final int TOP_RESOURCES_LIMIT = 10;

    @Override
    public OutputFormat format() {
        return OutputFormat.MARKDOWN;
    }

    @Override
    public void write(ProcessingConfig config, ProcessingResult result, Writer writer) throws IOException {
        List<LogRecord> records = new ArrayList<>();
        result.records().forEachRemaining(records::add);
        Statistics stats = result.statistics();

        IndentedWriter w = new IndentedWriter(writer);
        MarkdownRenderer renderer = new MarkdownRenderer(w);

        renderer.renderHeading(HEADING_LEVEL_1, "Incident Radar Report");

        renderConfiguration(renderer, config);

        renderer.renderHeading(HEADING_LEVEL_2, "Log Lines");
        renderLogLines(renderer, records);

        renderer.renderHeading(HEADING_LEVEL_2, "Sources");
        renderSources(renderer, result);

        renderStatistics(renderer, stats, records.size());
    }

    private void renderConfiguration(MarkdownRenderer renderer, ProcessingConfig config) throws IOException {
        renderer.renderHeading(HEADING_LEVEL_2, "Configuration");

        Map<String, String> data = new LinkedHashMap<>();
        data.put("Path", config.path());
        data.put("Output Format", config.outputFormat().name());
        addIfNotNull(data, "From", config.from());
        addIfNotNull(data, "To", config.to());

        renderer.renderKeyValueTable(data);
    }

    private void renderLogLines(MarkdownRenderer renderer, List<LogRecord> records) throws IOException {
        List<String> lines = records.stream()
                .map(r -> r.rawLine() != null ? r.rawLine() : formatLogRecord(r))
                .toList();
        renderer.renderLogLines(lines);
    }

    private void renderSources(MarkdownRenderer renderer, ProcessingResult result) throws IOException {
        renderer.renderSources(result.sources());

        Map<String, String> data = new LinkedHashMap<>();
        data.put("Source Count", String.valueOf(result.sources().size()));
        data.put("Lines Read", String.valueOf(result.linesRead()));
        data.put("Lines Skipped", String.valueOf(result.linesSkipped()));

        renderer.renderKeyValueTable(data);
    }

    private void renderStatistics(MarkdownRenderer renderer, Statistics stats, int count) throws IOException {
        renderer.renderHeading(HEADING_LEVEL_2, "Statistics");

        Map<String, String> mainMetrics = new LinkedHashMap<>();
        mainMetrics.put("Total Lines", String.valueOf(count));
        mainMetrics.put("Total Requests", String.valueOf(stats.totalRequests()));
        mainMetrics.put("Average Body Bytes", "%.2f".formatted(stats.averageBodyBytes()));
        mainMetrics.put("Max Body Bytes", String.valueOf(stats.maxBodyBytes()));
        mainMetrics.put("p95 Body Bytes", String.valueOf(stats.p95BodyBytes()));
        renderer.renderKeyValueTable(mainMetrics);

        renderer.renderHeading(HEADING_LEVEL_3, "Status Distribution");
        renderer.renderDistributionTable(stats.statusDistribution(), "Status", "Count");

        renderer.renderHeading(HEADING_LEVEL_3, "Top-10 Resources");
        renderer.renderDistributionTable(stats.topResources(TOP_RESOURCES_LIMIT), "Resource", "Count");
    }

    private String formatLogRecord(LogRecord record) {
        return "%s - - [%s] \"%s %s %s\" %d %d"
                .formatted(
                        record.ipAddress(),
                        record.timeLocal(),
                        record.method(),
                        record.resource(),
                        record.protocol(),
                        record.statusCode(),
                        record.bodyBytesSent());
    }

    private void addIfNotNull(Map<String, String> map, String key, @Nullable Object value) {
        if (value != null) {
            map.put(key, value.toString());
        }
    }
}
