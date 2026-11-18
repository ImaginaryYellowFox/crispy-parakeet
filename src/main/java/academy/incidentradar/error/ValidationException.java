package academy.incidentradar.error;

public class ValidationException extends IncidentRadarException {
    public ValidationException(String message) {
        super(message, ExitCodes.VALIDATION);
    }
}
