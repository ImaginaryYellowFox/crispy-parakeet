package academy.incidentradar.error;

import org.jspecify.annotations.Nullable;

public class IncidentRadarException extends IllegalArgumentException {
    private final int exitCode;

    public IncidentRadarException(String message, int exitCode) {
        this(message, exitCode, null);
    }

    public IncidentRadarException(String message, int exitCode, @Nullable Throwable cause) {
        super(message, cause);
        this.exitCode = exitCode;
    }

    public int getCode() {
        return exitCode;
    }
}
