package academy.incidentradar.config;

import java.time.LocalDate;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

public record ProcessingConfig(
        String path,
        OutputFormat outputFormat,
        @Nullable String outputPath,
        @Nullable LocalDate from,
        @Nullable LocalDate to) {

    public ProcessingConfig {
        Objects.requireNonNull(path, "path must not be null");
        Objects.requireNonNull(outputFormat, "outputFormat must not be null");
    }
}
