package academy.incidentradar.source;

import academy.incidentradar.error.SourceException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;

public final class UrlLogSource implements LogSource {

    private static final int HTTP_NOT_FOUND = 404;
    private static final int HTTP_CLIENT_ERROR = 400;

    private final String urlString;

    @Nullable
    private Stream<String> stream;

    public UrlLogSource(String urlString) {
        this.urlString = urlString;
    }

    @Override
    public Stream<String> stream() {
        if (stream != null) {
            throw new IllegalStateException("Источник уже использован: " + urlString);
        }

        HttpResponse<String> response = fetchResponse();
        validateResponse(response);

        this.stream = response.body().lines();
        return stream;
    }

    private HttpResponse<String> fetchResponse() {
        HttpRequest request = HttpRequest.newBuilder(URI.create(urlString))
                .GET()
                .header("User-Agent", "IncidentRadar/1.0")
                .build();

        try {
            return HttpClients.shared().send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new SourceException("Не удалось подключиться к URL: " + urlString, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SourceException("Запрос к URL прерван: " + urlString, e);
        }
    }

    private void validateResponse(HttpResponse<String> response) {
        int code = response.statusCode();
        if (code == HTTP_NOT_FOUND) {
            throw new SourceException("URL вернул 404: " + urlString);
        }
        if (code >= HTTP_CLIENT_ERROR) {
            throw new SourceException("URL вернул код " + code + ": " + urlString);
        }
    }

    @Override
    public List<String> sources() {
        return List.of(urlString);
    }

    @Override
    public void close() {
        if (stream != null) {
            stream.close();
        }
    }
}
