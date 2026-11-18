package academy.incidentradar.mandatory.mr1;

import static org.assertj.core.api.Assertions.assertThat;

import academy.incidentradar.support.CliRunner;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class TestUtils {

    public static final String SAMPLE_LOG =
            "127.0.0.1 - - [10/Oct/2000:13:55:36 -0700] \"GET / HTTP/1.0\" 200 1 \"-\" \"-\"";

    private TestUtils() {}

    public static Path createFile(Path dir, String filename, String content) throws IOException {
        Path file = dir.resolve(filename);
        Files.writeString(file, content);
        return file;
    }

    public static void assertCliError(CliRunner.Result result, String expectedStderrPart) {
        assertCliError(result, 2, expectedStderrPart);
    }

    public static void assertCliError(CliRunner.Result result, int expectedCode, String expectedStderrPart) {
        assertThat(result.exitCode())
                .as("Ожидался код ошибки %d. Вывод: %s", expectedCode, result)
                .isEqualTo(expectedCode);
        assertThat(result.stderr())
                .as("В stderr должно быть сообщение: " + expectedStderrPart)
                .containsIgnoringCase(expectedStderrPart);
    }

    public static HttpServer createServer(String path, String response, int statusCode) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.createContext(path, exchange -> {
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(statusCode, statusCode == 200 && bytes.length > 0 ? bytes.length : -1);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        });
        server.start();
        return server;
    }
}
