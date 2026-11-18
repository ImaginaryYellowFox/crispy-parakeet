package academy.incidentradar.parse;

import academy.incidentradar.model.LogRecord;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;

public final class LogLineParser {

    private static final Pattern LOG_LINE = Pattern.compile(
            "^(\\S+) (\\S+) (\\S+) \\[([^\\]]+)] \"([^\"]*)\" (\\d{3}) (\\d+) \"([^\"]*)\" \"([^\"]*)\"$");

    private static final Pattern REQUEST = Pattern.compile("^(\\S+) (\\S+) (\\S+)$");

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z", Locale.ENGLISH);

    private static final String DASH = "-";

    private static final int GROUP_IP = 1;
    private static final int GROUP_REMOTE_USER = 2;
    private static final int GROUP_TIME = 4;
    private static final int GROUP_REQUEST = 5;
    private static final int GROUP_STATUS = 6;
    private static final int GROUP_BYTES = 7;
    private static final int GROUP_REFERER = 8;
    private static final int GROUP_USER_AGENT = 9;

    private static final int GROUP_METHOD = 1;
    private static final int GROUP_RESOURCE = 2;
    private static final int GROUP_PROTOCOL = 3;

    public LogRecord parse(String line) {
        Matcher m = LOG_LINE.matcher(line);
        if (!m.matches()) {
            throw new MalformedLineException("Строка не соответствует формату NGINX-лога: " + line);
        }

        String request = m.group(GROUP_REQUEST);
        Matcher rm = REQUEST.matcher(request);
        if (!rm.matches()) {
            throw new MalformedLineException("Не удалось разобрать request: " + request);
        }

        return buildRecord(line, m, rm);
    }

    private LogRecord buildRecord(String line, Matcher m, Matcher rm) {
        return LogRecord.builder()
                .rawLine(line)
                .ipAddress(m.group(GROUP_IP))
                .remoteUser(dashToNull(m.group(GROUP_REMOTE_USER)))
                .timeLocal(parseTime(m.group(GROUP_TIME)))
                .method(rm.group(GROUP_METHOD))
                .resource(rm.group(GROUP_RESOURCE))
                .protocol(rm.group(GROUP_PROTOCOL))
                .statusCode(parseStatus(m.group(GROUP_STATUS)))
                .bodyBytesSent(parseBytes(m.group(GROUP_BYTES)))
                .httpReferer(dashToNull(m.group(GROUP_REFERER)))
                .userAgent(dashToNull(m.group(GROUP_USER_AGENT)))
                .build();
    }

    private ZonedDateTime parseTime(String timeStr) {
        try {
            return ZonedDateTime.parse(timeStr, TIME_FORMAT);
        } catch (DateTimeParseException e) {
            throw new MalformedLineException("Не удалось разобрать дату: " + timeStr, e);
        }
    }

    private int parseStatus(String statusStr) {
        try {
            return Integer.parseInt(statusStr);
        } catch (NumberFormatException e) {
            throw new MalformedLineException("Не удалось разобрать статус: " + statusStr, e);
        }
    }

    private long parseBytes(String bytesStr) {
        try {
            return Long.parseLong(bytesStr);
        } catch (NumberFormatException e) {
            throw new MalformedLineException("Не удалось разобрать размер ответа: " + bytesStr, e);
        }
    }

    private static @Nullable String dashToNull(@Nullable String v) {
        return (v == null || v.isEmpty() || DASH.equals(v)) ? null : v;
    }
}
