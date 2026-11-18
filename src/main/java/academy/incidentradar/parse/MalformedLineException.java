package academy.incidentradar.parse;

public class MalformedLineException extends RuntimeException {
    public MalformedLineException(String message) {
        super(message);
    }

    public MalformedLineException(String message, Throwable cause) {
        super(message, cause);
    }
}
