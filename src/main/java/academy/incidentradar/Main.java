package academy.incidentradar;

import academy.incidentradar.cli.IncidentRadarCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

/** Точка входа в Incident Radar. */
public final class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    private Main() {}

    public static void main(String[] args) {
        LOG.debug("Аргументы запуска: {}", String.join(" ", args));
        System.exit(new CommandLine(new IncidentRadarCommand()).execute(args));
    }
}
