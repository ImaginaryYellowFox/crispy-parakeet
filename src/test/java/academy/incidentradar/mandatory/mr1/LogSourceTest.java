package academy.incidentradar.mandatory.mr1;

import static academy.incidentradar.mandatory.mr1.TestUtils.SAMPLE_LOG;
import static academy.incidentradar.mandatory.mr1.TestUtils.assertCliError;
import static academy.incidentradar.mandatory.mr1.TestUtils.createFile;
import static org.assertj.core.api.Assertions.assertThat;

import academy.incidentradar.error.ExitCodes;
import academy.incidentradar.source.FileLogSource;
import academy.incidentradar.support.CliRunner;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@DisplayName("MR1. Источники логов")
class LogSourceTest {

    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    @DisplayName("Локальный файл читается")
    void readsLocalFile(@TempDir Path tempDir) throws IOException {
        Path logFile = createFile(tempDir, "test.log", SAMPLE_LOG + "\n");

        var result = CliRunner.run("--path", logFile.toString(), "-f", "markdown");

        assertThat(result.exitCode()).isZero();
        assertThat(result.stdout()).contains("127.0.0.1 - - [10/Oct/2000:13:55:36 -0700]");
    }

    @Test
    @DisplayName("Glob-маска: FileLogSource читает все подошедшие файлы")
    void readsAllFilesMatchingGlob(@TempDir Path tempDir) throws IOException {
        createFile(tempDir, "access-1.log", "Line from file 1\n");
        createFile(tempDir, "access-2.log", "Line from file 2\n");
        createFile(tempDir, "ignored.txt", "Line from ignored\n");

        String globPath = tempDir.toAbsolutePath() + java.io.File.separator + "*.log";

        try (var source = new FileLogSource(globPath);
                var lines = source.stream()) {
            List<String> result = lines.toList();
            assertThat(result).contains("Line from file 1", "Line from file 2").doesNotContain("Line from ignored");
        }
    }

    @Test
    @DisplayName("Лог читается по URL")
    void readsRemoteFile() throws IOException {
        server = TestUtils.createServer("/test.log", SAMPLE_LOG + "\n", HttpURLConnection.HTTP_OK);
        String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/test.log";

        var result = CliRunner.run("--path", url, "-f", "markdown");

        assertThat(result.exitCode()).isZero();
        assertThat(result.stdout()).contains("127.0.0.1 - - [10/Oct/2000:13:55:36 -0700]");
    }

    @Test
    @DisplayName("URL вернул 404 -> exit code 2")
    void remote404ExitsWithTwo(@TempDir Path tempDir) throws IOException {
        Path logFile = createFile(tempDir, "test.log", SAMPLE_LOG);

        var result = CliRunner.run(
                "--path", logFile.toString(),
                "--from", "2025-03-02",
                "--to", "2025-03-01");

        assertCliError(result, ExitCodes.VALIDATION, "--from");
    }

    @Test
    @DisplayName("Успешная обработка -> exit code 0 (OK)")
    void successfulRunExitsWithOk(@TempDir Path tempDir) throws IOException {
        Path logFile = createFile(tempDir, "test.log", SAMPLE_LOG + "\n");

        var result = CliRunner.run("--path", logFile.toString());

        assertThat(result.exitCode())
                .as("Ожидался код %d (OK). Вывод: %s", ExitCodes.OK, result)
                .isEqualTo(ExitCodes.OK);
    }

    @Test
    @DisplayName("Несуществующий файл -> exit code 3 (SOURCE)")
    void missingFileExitsWithSource(@TempDir Path tempDir) {
        Path nonExistentFile = tempDir.resolve("no-such-file.log");

        var result = CliRunner.run("--path", nonExistentFile.toString());

        assertCliError(result, ExitCodes.SOURCE, "файл не найден");
    }

    @Test
    @DisplayName("URL вернул 404 -> exit code 3 (SOURCE)")
    void url404ExitsWithSource() throws IOException {
        var server = TestUtils.createServer("/not-found.log", "", HttpURLConnection.HTTP_NOT_FOUND);
        try {
            String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/not-found.log";
            var result = CliRunner.run("--path", url);

            assertCliError(result, ExitCodes.SOURCE, "404");
        } finally {
            server.stop(0);
        }
    }

    @Test
    @DisplayName("Файл отчёта уже существует -> exit code 4 (IO)")
    void existingOutputFileExitsWithIo(@TempDir Path tempDir) throws IOException {
        Path logFile = createFile(tempDir, "test.log", SAMPLE_LOG + "\n");
        Path reportFile = tempDir.resolve("report.md");

        Files.writeString(reportFile, "already exists");

        var result = CliRunner.run(
                "--path", logFile.toString(),
                "--output-path", reportFile.toString());

        assertCliError(result, ExitCodes.IO, "уже существует");
    }

    @Test
    @DisplayName("Указан несуществующий путь для отчёта -> exit code 4 (IO)")
    void missingOutputDirectoryExitsWithIo(@TempDir Path tempDir) throws IOException {
        Path logFile = createFile(tempDir, "test.log", SAMPLE_LOG + "\n");

        Path invalidReportPath = tempDir.resolve("no-such-dir").resolve("report.md");

        var result = CliRunner.run(
                "--path", logFile.toString(),
                "--output-path", invalidReportPath.toString());

        assertCliError(result, ExitCodes.IO, "Ошибка выполнения");
    }
}
