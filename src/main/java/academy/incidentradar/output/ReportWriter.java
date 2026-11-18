package academy.incidentradar.output;

import academy.incidentradar.config.OutputFormat;
import academy.incidentradar.config.ProcessingConfig;
import academy.incidentradar.processing.ProcessingResult;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public interface ReportWriter {

    OutputFormat format();

    void write(ProcessingConfig config, ProcessingResult result, Writer writer) throws IOException;

    default void write(ProcessingConfig config, ProcessingResult result, Path outputPath) throws IOException {
        try (Writer writer = Files.newBufferedWriter(
                outputPath, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
            write(config, result, writer);
        }
    }

    default String escape(String value) {
        if (value == null) {
            return "null";
        }
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
