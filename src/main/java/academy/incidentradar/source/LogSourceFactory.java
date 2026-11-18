package academy.incidentradar.source;

import academy.incidentradar.validation.ConfigValidator;

public final class LogSourceFactory {
    private LogSourceFactory() {}

    public static LogSource create(String path) {
        if (ConfigValidator.isLocalPath(path)) {
            return new FileLogSource(path);
        }
        return new UrlLogSource(path);
    }
}
