package academy.incidentradar.model;

import java.time.ZonedDateTime;
import java.util.Arrays;
import org.jspecify.annotations.Nullable;

public final class LogRecord {

    private final @Nullable String rawLine;
    private final String ipAddress;
    private final @Nullable String remoteUser;
    private final ZonedDateTime timeLocal;
    private final String method;
    private final String resource;
    private final String protocol;
    private final int statusCode;
    private final long bodyBytesSent;
    private final @Nullable String httpReferer;
    private final @Nullable String userAgent;

    @SuppressWarnings("NullAway")
    LogRecord(LogRecordBuilder b) {
        this.rawLine = b.getRawLine();
        this.ipAddress = b.getIpAddress();
        this.timeLocal = b.getTimeLocal();
        this.method = b.getMethod();
        this.resource = b.getResource();
        this.protocol = b.getProtocol();

        this.remoteUser = b.getRemoteUser();
        this.statusCode = b.getStatusCode();
        this.bodyBytesSent = b.getBodyBytesSent();
        this.httpReferer = b.getHttpReferer();
        this.userAgent = b.getUserAgent();
    }

    static LogRecord create(LogRecordBuilder b) {
        return new LogRecord(b);
    }

    public static LogRecordBuilder builder() {
        return new LogRecordBuilder();
    }

    public @Nullable String rawLine() {
        return rawLine;
    }

    public String ipAddress() {
        return ipAddress;
    }

    public @Nullable String remoteUser() {
        return remoteUser;
    }

    public ZonedDateTime timeLocal() {
        return timeLocal;
    }

    public String method() {
        return method;
    }

    public String resource() {
        return resource;
    }

    public String protocol() {
        return protocol;
    }

    public int statusCode() {
        return statusCode;
    }

    public long bodyBytesSent() {
        return bodyBytesSent;
    }

    public @Nullable String httpReferer() {
        return httpReferer;
    }

    public @Nullable String userAgent() {
        return userAgent;
    }

    @Override
    public String toString() {
        return LogRecordFormatter.format(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LogRecord other)) {
            return false;
        }
        return Arrays.equals(this.getFieldsArray(), other.getFieldsArray());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(getFieldsArray());
    }

    private Object[] getFieldsArray() {
        return new Object[] {
            ipAddress,
            remoteUser,
            timeLocal,
            method,
            resource,
            protocol,
            statusCode,
            bodyBytesSent,
            httpReferer,
            userAgent,
            rawLine
        };
    }
}
