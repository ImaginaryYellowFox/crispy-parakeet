package academy.incidentradar.cli;

import academy.incidentradar.config.OutputFormat;
import academy.incidentradar.config.ProcessingConfig;
import academy.incidentradar.error.ExitCodes;
import academy.incidentradar.error.IncidentRadarException;
import academy.incidentradar.filter.DateFilter;
import academy.incidentradar.output.JsonReportWriter;
import academy.incidentradar.output.MarkdownReportWriter;
import academy.incidentradar.output.ReportWriter;
import academy.incidentradar.processing.LogProcessor;
import academy.incidentradar.processing.ProcessingResult;
import academy.incidentradar.source.LogSource;
import academy.incidentradar.source.LogSourceFactory;
import academy.incidentradar.validation.ConfigValidator;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(
        name = "incident-radar",
        mixinStandardHelpOptions = true,
        version = "1.0.0",
        description = "Анализ NGINX-логов: чтение, фильтрация, генерация отчётов.")
public class IncidentRadarCommand implements Callable<Integer> {

    @Option(
            names = {"--path", "-p"},
            description = "Файл, glob-маска или URL с логами",
            required = true)
    @Nullable
    private String path;

    @Option(
            names = {"--output-format", "-f"},
            description = "Формат отчёта: json или markdown",
            defaultValue = "markdown")
    @Nullable
    private String outputFormat;

    @Option(
            names = {"--output-path", "-o"},
            description = "Путь для сохранения отчёта")
    @Nullable
    private String outputPath;

    @Option(
            names = {"--from"},
            description = "Начало периода (YYYY-MM-DD)")
    @Nullable
    private LocalDate from;

    @Option(
            names = {"--to"},
            description = "Конец периода (YYYY-MM-DD)")
    @Nullable
    private LocalDate to;

    @Parameters(index = "0", description = "Позиционный аргумент — альтернатива --path", arity = "0..1")
    private List<String> positionalArgs = new ArrayList<>();

    @Override
    public Integer call() {
        try {
            process(createConfig());
            return ExitCodes.OK;
        } catch (IncidentRadarException e) {
            System.err.println("Ошибка: " + e.getMessage());
            return e.getCode();
        } catch (FileAlreadyExistsException e) {
            System.err.println("Ошибка выполнения: файл уже существует: " + e.getFile());
            return ExitCodes.IO;
        } catch (IOException e) {
            System.err.println("Ошибка выполнения: " + e.getMessage());
            return ExitCodes.IO;
        } catch (IllegalArgumentException e) {
            System.err.println("Ошибка: " + e.getMessage());
            return ExitCodes.VALIDATION;
        }
    }

    private ProcessingConfig createConfig() {
        String resolvedPath = path != null ? path : (positionalArgs.isEmpty() ? null : positionalArgs.get(0));

        if (resolvedPath == null) {
            throw new IllegalArgumentException("не передан обязательный параметр --path");
        }
        if (outputFormat == null) {
            throw new IllegalArgumentException("формат вывода не указан");
        }

        OutputFormat format;
        try {
            format = OutputFormat.valueOf(outputFormat.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("неподдерживаемый формат вывода: " + outputFormat);
        }

        String resolvedOutputPath = (outputPath != null && !outputPath.isBlank()) ? outputPath : null;
        ProcessingConfig config = new ProcessingConfig(resolvedPath, format, resolvedOutputPath, from, to);

        ConfigValidator.validate(config);
        return config;
    }

    private void process(ProcessingConfig config) throws IOException {
        try (LogSource logSource = LogSourceFactory.create(config.path());
                Stream<String> rawStream = logSource.stream()) {

            ProcessingResult result = runProcessing(rawStream, config, logSource);
            writeReport(result, config);
        }
    }

    private ProcessingResult runProcessing(Stream<String> rawStream, ProcessingConfig config, LogSource logSource) {
        DateFilter filter = new DateFilter(config.from(), config.to());
        return new LogProcessor().process(rawStream, filter, logSource.sources());
    }

    private void writeReport(ProcessingResult result, ProcessingConfig config) throws IOException {
        ReportWriter writer = createReportWriter(config.outputFormat());

        if (config.outputPath() != null) {
            writer.write(config, result, Paths.get(config.outputPath()));
        } else {
            writer.write(config, result, createConsoleWriter());
        }
    }

    private ReportWriter createReportWriter(OutputFormat format) {
        return format == OutputFormat.JSON ? new JsonReportWriter() : new MarkdownReportWriter();
    }

    private Writer createConsoleWriter() {
        return new PrintWriter(new OutputStreamWriter(System.out, StandardCharsets.UTF_8), true);
    }
}
