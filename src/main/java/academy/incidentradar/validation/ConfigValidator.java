package academy.incidentradar.validation;

import academy.incidentradar.config.OutputFormat;
import academy.incidentradar.config.ProcessingConfig;
import academy.incidentradar.error.SourceException;
import academy.incidentradar.error.ValidationException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.jspecify.annotations.Nullable;

public final class ConfigValidator {

    private static final String[] SUPPORTED_EXTENSIONS = {".log", ".txt"};
    private static final String HTTP_PREFIX = "http://";
    private static final String HTTPS_PREFIX = "https://";
    private static final String JSON_EXTENSION = ".json";
    private static final String MD_EXTENSION = ".md";

    private ConfigValidator() {}

    public static void validate(ProcessingConfig config) {
        validatePathExists(config.path());
        validateOutputFormat(config.outputFormat());
        validateOutputExtension(config.outputPath(), config.outputFormat());
        validateDateRange(config);
    }

    private static void validatePathExists(String path) {
        if (!isLocalPath(path)) {
            return;
        }

        if (!isGlobPattern(path) && !isSupportedExtension(path)) {
            throw new IllegalArgumentException("Неподдерживаемое расширение файла: " + path + ". Поддерживаются: "
                    + String.join(", ", SUPPORTED_EXTENSIONS));
        }

        Path p = Paths.get(path);
        if (!p.toFile().exists()) {
            throw new SourceException("Файл не найден: " + path);
        }
    }

    private static void validateOutputFormat(@Nullable OutputFormat format) {
        if (format == null) {
            throw new IllegalArgumentException("Формат вывода не указан");
        }
    }

    private static void validateOutputExtension(@Nullable String outputPath, @Nullable OutputFormat format) {
        if (outputPath == null || outputPath.isBlank() || format == null) {
            return;
        }

        String expectedExtension = getExpectedExtension(format);
        if (!outputPath.toLowerCase().endsWith(expectedExtension)) {
            throw new IllegalArgumentException("Расширение файла отчёта '" + outputPath + "' не соответствует формату "
                    + format.name().toLowerCase());
        }
    }

    private static String getExpectedExtension(OutputFormat format) {
        return switch (format) {
            case JSON -> JSON_EXTENSION;
            case MARKDOWN -> MD_EXTENSION;
        };
    }

    private static void validateDateRange(ProcessingConfig config) {
        var from = config.from();
        var to = config.to();
        if (from != null && to != null && !from.isBefore(to)) {
            throw new ValidationException("--from (" + from + ") должен быть строго меньше --to (" + to + ")");
        }
    }

    public static boolean isLocalPath(String path) {
        String lower = path.toLowerCase();
        return !lower.startsWith(HTTP_PREFIX) && !lower.startsWith(HTTPS_PREFIX);
    }

    public static boolean isGlobPattern(String path) {
        return path.contains("*") || path.contains("?") || path.contains("[");
    }

    public static boolean isSupportedExtension(String fileName) {
        String lower = fileName.toLowerCase();
        for (String ext : SUPPORTED_EXTENSIONS) {
            if (lower.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }
}
