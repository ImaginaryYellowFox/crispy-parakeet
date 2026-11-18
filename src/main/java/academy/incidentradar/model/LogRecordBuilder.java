package academy.incidentradar.model;

import java.time.ZonedDateTime;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

public final class LogRecordBuilder {
    private @Nullable String rawLine;
    private @Nullable String ipAddress;
    private @Nullable String remoteUser;
    private @Nullable ZonedDateTime timeLocal;
    private @Nullable String method;
    private @Nullable String resource;
    private @Nullable String protocol;
    private int statusCode;
    private long bodyBytesSent;
    private @Nullable String httpReferer;
    private @Nullable String userAgent;

    public LogRecordBuilder rawLine(@Nullable String v) {
        this.rawLine = v;
        return this;
    }

    public LogRecordBuilder ipAddress(String v) {
        this.ipAddress = v;
        return this;
    }

    public LogRecordBuilder remoteUser(@Nullable String v) {
        this.remoteUser = v;
        return this;
    }

    public LogRecordBuilder timeLocal(ZonedDateTime v) {
        this.timeLocal = v;
        return this;
    }

    public LogRecordBuilder method(String v) {
        this.method = v;
        return this;
    }

    public LogRecordBuilder resource(String v) {
        this.resource = v;
        return this;
    }

    public LogRecordBuilder protocol(String v) {
        this.protocol = v;
        return this;
    }

    public LogRecordBuilder statusCode(int v) {
        this.statusCode = v;
        return this;
    }

    public LogRecordBuilder bodyBytesSent(long v) {
        this.bodyBytesSent = v;
        return this;
    }

    public LogRecordBuilder httpReferer(@Nullable String v) {
        this.httpReferer = v;
        return this;
    }

    public LogRecordBuilder userAgent(@Nullable String v) {
        this.userAgent = v;
        return this;
    }

    public LogRecord build() {
        Objects.requireNonNull(ipAddress, "ipAddress");
        Objects.requireNonNull(timeLocal, "timeLocal");
        Objects.requireNonNull(method, "method");
        Objects.requireNonNull(resource, "resource");
        Objects.requireNonNull(protocol, "protocol");

        return LogRecord.create(this);
    }

    @Nullable
    String getRawLine() {
        return rawLine;
    }

    String getIpAddress() {
        return Objects.requireNonNull(ipAddress, "ipAddress");
    }

    @Nullable
    String getRemoteUser() {
        return remoteUser;
    }

    ZonedDateTime getTimeLocal() {
        return Objects.requireNonNull(timeLocal, "timeLocal");
    }

    String getMethod() {
        return Objects.requireNonNull(method, "method");
    }

    String getResource() {
        return Objects.requireNonNull(resource, "resource");
    }

    String getProtocol() {
        return Objects.requireNonNull(protocol, "protocol");
    }

    int getStatusCode() {
        return statusCode;
    }

    long getBodyBytesSent() {
        return bodyBytesSent;
    }

    @Nullable
    String getHttpReferer() {
        return httpReferer;
    }

    @Nullable
    String getUserAgent() {
        return userAgent;
    }
}
