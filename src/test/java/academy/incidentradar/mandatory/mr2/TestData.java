package academy.incidentradar.mandatory.mr2;

import academy.incidentradar.model.LogRecord;
import academy.incidentradar.stats.Statistics;
import java.time.LocalDate;
import java.time.ZonedDateTime;

public final class TestData {

    public static final LocalDate DAY_1 = LocalDate.of(2025, 3, 1);
    public static final LocalDate DAY_2 = LocalDate.of(2025, 3, 2);
    public static final LocalDate DAY_3 = LocalDate.of(2025, 3, 3);

    public static final String VALID_LINE =
            "93.180.71.3 - - [17/May/2015:08:05:32 +0000] \"GET /downloads/product_1 HTTP/1.1\" 304 0 \"-\" \"Debian APT-HTTP/1.3\"";

    public static final String POST_LINE =
            "127.0.0.1 - - [10/Oct/2000:13:55:36 -0700] \"POST /api/v1/users HTTP/2.0\" 201 150 \"-\" \"Mozilla/5.0\"";

    public static final String BAD_LINE = "это не лог nginx, тут нет нужных полей";

    public static final ZonedDateTime VALID_LINE_TIME = ZonedDateTime.parse("2015-05-17T08:05:32Z");
    public static final ZonedDateTime POST_LINE_TIME = ZonedDateTime.parse("2000-10-10T13:55:36-07:00");

    public static final LogRecord EXPECTED_RECORD = LogRecord.builder()
            .ipAddress("93.180.71.3")
            .timeLocal(VALID_LINE_TIME)
            .method("GET")
            .resource("/downloads/product_1")
            .protocol("HTTP/1.1")
            .statusCode(304)
            .bodyBytesSent(0)
            .userAgent("Debian APT-HTTP/1.3")
            .rawLine(VALID_LINE)
            .build();

    private TestData() {}

    public static LogRecord createRecord(LocalDate date) {
        return LogRecord.builder()
                .ipAddress("127.0.0.1")
                .timeLocal(ZonedDateTime.parse(date + "T12:00:00Z"))
                .method("GET")
                .resource("/")
                .protocol("HTTP/1.1")
                .statusCode(200)
                .bodyBytesSent(0)
                .build();
    }

    public static LogRecord createRecord(int status, String resource, long bytes) {
        return LogRecord.builder()
                .ipAddress("127.0.0.1")
                .timeLocal(ZonedDateTime.parse("2025-03-01T12:00:00Z"))
                .method("GET")
                .resource(resource)
                .protocol("HTTP/1.1")
                .statusCode(status)
                .bodyBytesSent(bytes)
                .build();
    }

    public static void addStatusCodes(Statistics stats, int... statusCodes) {
        for (int statusCode : statusCodes) {
            stats.accept(createRecord(statusCode, "/", 10));
        }
    }

    public static void addResources(Statistics stats, String... resources) {
        for (String resource : resources) {
            stats.accept(createRecord(200, resource, 0));
        }
    }

    public static void addSizes(Statistics stats, long... sizes) {
        for (long size : sizes) {
            stats.accept(createRecord(200, "/", size));
        }
    }
}
