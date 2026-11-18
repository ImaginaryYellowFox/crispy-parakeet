package academy.incidentradar.source;

import java.net.http.HttpClient;
import java.time.Duration;

public final class HttpClients {
    private static final HttpClient SHARED =
            HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    private HttpClients() {}

    public static HttpClient shared() {
        return SHARED;
    }
}
