package academy.incidentradar.output;

import academy.incidentradar.config.OutputFormat;
import academy.incidentradar.config.ProcessingConfig;
import academy.incidentradar.model.LogRecord;
import academy.incidentradar.processing.ProcessingResult;
import academy.incidentradar.stats.Statistics;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class JsonReportWriter implements ReportWriter {

    private static final int TOP_RESOURCES_LIMIT = 10;

    @Override
    public OutputFormat format() {
        return OutputFormat.JSON;
    }

    @Override
    public void write(ProcessingConfig config, ProcessingResult result, Writer writer) throws IOException {
        IndentedWriter w = new IndentedWriter(writer);

        w.writeln("{");
        writeMetadata(config, w);
        int count = writeLogLines(result.records(), w);

        writeSourcesBlock(result, w);
        writeStatistics(result.statistics(), count, w);
        w.write("}");
    }

    private void writeMetadata(ProcessingConfig config, IndentedWriter w) throws IOException {
        w.writeln("  \"path\": \"" + escape(config.path()) + "\",");
        w.writeln("  \"format\": \"" + config.outputFormat() + "\",");

        var from = config.from();
        if (from != null) {
            w.writeln("  \"from\": \"" + from + "\",");
        }

        var to = config.to();
        if (to != null) {
            w.writeln("  \"to\": \"" + to + "\",");
        }
    }

    private int writeLogLines(Iterator<LogRecord> lines, IndentedWriter w) throws IOException {
        w.writeln("  \"lines\": [");
        int count = 0;
        while (lines.hasNext()) {
            LogRecord record = lines.next();
            if (count > 0) {
                w.writeln(",");
            }

            w.writeln("    {");
            w.writeln("      \"ip\": \"" + escape(record.ipAddress()) + "\",");
            w.writeln("      \"status\": " + record.statusCode() + ",");
            w.writeln("      \"bytes\": " + record.bodyBytesSent());
            w.write("    }");
            count++;
        }
        w.writeln("  ],");
        return count;
    }

    private void writeSourcesBlock(ProcessingResult result, IndentedWriter w) throws IOException {
        w.writeln("  \"sourceCount\": " + result.sources().size() + ",");
        w.writeln("  \"sources\": [");

        List<String> sources = result.sources();
        for (int i = 0; i < sources.size(); i++) {
            w.write("    \"" + escape(sources.get(i)) + "\"");
            if (i < sources.size() - 1) {
                w.writeln(",");
            } else {
                w.writeln();
            }
        }

        w.writeln("  ],");
        w.writeln("  \"linesRead\": " + result.linesRead() + ",");
        w.writeln("  \"linesSkipped\": " + result.linesSkipped() + ",");
    }

    private void writeStatistics(Statistics stats, int totalLines, IndentedWriter w) throws IOException {
        w.writeln("  \"totalLines\": " + totalLines + ",");
        w.writeln("  \"totalRequests\": " + stats.totalRequests() + ",");
        w.writeln("  \"statusDistribution\": " + jsonMapIntLong(stats.statusDistribution()) + ",");
        w.writeln("  \"topResources\": " + jsonMapStringLong(stats.topResources(TOP_RESOURCES_LIMIT)) + ",");

        w.writeln("  \"bodyBytes\": {");
        w.writeln("    \"average\": " + "%.2f".formatted(stats.averageBodyBytes()) + ",");
        w.writeln("    \"max\": " + stats.maxBodyBytes() + ",");
        w.writeln("    \"p95\": " + stats.p95BodyBytes());
        w.writeln("  }");
    }

    private String jsonMapIntLong(Map<Integer, Long> map) {
        return map.entrySet().stream()
                .map(e -> "\"" + e.getKey() + "\": " + e.getValue())
                .collect(Collectors.joining(", ", "{", "}"));
    }

    private String jsonMapStringLong(Map<String, Long> map) {
        return map.entrySet().stream()
                .map(e -> "\"" + escape(e.getKey()) + "\": " + e.getValue())
                .collect(Collectors.joining(", ", "{", "}"));
    }
}
