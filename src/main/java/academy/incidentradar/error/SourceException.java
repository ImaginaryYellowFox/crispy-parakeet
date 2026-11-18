package academy.incidentradar.error;

import org.jspecify.annotations.Nullable;

public class SourceException extends IncidentRadarException {

    public SourceException(String message) {
        this(message, null);
    }

    public SourceException(String message, @Nullable Throwable cause) {
        super(message, ExitCodes.SOURCE, cause);
    }
}
