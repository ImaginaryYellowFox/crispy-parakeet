package academy.incidentradar.model;

public final class LogRecordFormatter {

    private LogRecordFormatter() {}

    public static String format(LogRecord r) {
        return String.format(
                "LogRecord{ip='%s', user='%s', time=%s, req='%s %s %s', status=%d, bytes=%d, referer='%s', agent='%s', raw='%s'}",
                r.ipAddress(),
                r.remoteUser(),
                r.timeLocal(),
                r.method(),
                r.resource(),
                r.protocol(),
                r.statusCode(),
                r.bodyBytesSent(),
                r.httpReferer(),
                r.userAgent(),
                r.rawLine());
    }
}
