package academy.incidentradar.mandatory.mr1;

import static academy.incidentradar.mandatory.mr1.TestUtils.SAMPLE_LOG;
import static academy.incidentradar.mandatory.mr1.TestUtils.assertCliError;
import static academy.incidentradar.mandatory.mr1.TestUtils.createFile;
import static org.assertj.core.api.Assertions.assertThat;

import academy.incidentradar.error.ExitCodes;
import academy.incidentradar.support.CliRunner;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("MR1. Валидация аргументов CLI")
class CliValidationTest {

    @Test
    @DisplayName("--from больше или равен --to -> exit code 2")
    void fromNotBeforeToExitsWithTwo(@TempDir Path tempDir) throws Exception {
        Path logFile = createFile(tempDir, "test.log", SAMPLE_LOG);

        var result = CliRunner.run(
                "--path", logFile.toString(),
                "--from", "2025-03-02",
                "--to", "2025-03-01");

        assertCliError(result, "--from");
    }

    @ParameterizedTest
    @ValueSource(strings = {"xml", "csv", "txt"})
    @DisplayName("Неподдерживаемый --output-format \"{0}\" -> exit code 2")
    void unsupportedOutputFormatExitsWithTwo(String format) {
        var result = CliRunner.run("--path", "test.log", "--output-format", format);
        assertCliError(result, "неподдерживаемый формат");
    }

    @Test
    @DisplayName("Несуществующий локальный файл -> exit code 3 (SOURCE)")
    void missingLocalFileExitsWithSource() {
        var result = CliRunner.run("--path", "non-existent-file-12345.log");
        assertCliError(result, ExitCodes.SOURCE, "файл не найден");
    }

    @Test
    @DisplayName("Расширение файла отчёта не соответствует формату -> exit code 2")
    void outputExtensionMismatchExitsWithTwo(@TempDir Path tempDir) throws Exception {
        Path logFile = createFile(tempDir, "test.log", SAMPLE_LOG);
        Path outputFile = tempDir.resolve("report.md");

        var result = CliRunner.run(
                "--path", logFile.toString(),
                "--output-format", "json",
                "--output-path", outputFile.toString());

        assertCliError(result, "не соответствует формату");
    }

    @Test
    @DisplayName("Файл с расширением .txt поддерживается -> exit code 0")
    void txtExtensionIsSupported(@TempDir Path tempDir) throws Exception {
        Path txtFile = createFile(tempDir, "test.txt", SAMPLE_LOG);

        var result = CliRunner.run("--path", txtFile.toString());

        assertThat(result.exitCode()).isZero();
    }
}
