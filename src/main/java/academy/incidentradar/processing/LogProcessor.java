package academy.incidentradar.processing;

import academy.incidentradar.filter.DateFilter;
import academy.incidentradar.model.LogRecord;
import academy.incidentradar.parse.LogLineParser;
import academy.incidentradar.parse.MalformedLineException;
import academy.incidentradar.stats.Statistics;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LogProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(LogProcessor.class);

    private final LogLineParser parser = new LogLineParser();

    public ProcessingResult process(Stream<String> rawStream, DateFilter filter, List<String> sources) {
        Statistics stats = new Statistics();
        LongAdder read = new LongAdder();
        LongAdder skipped = new LongAdder();

        Iterator<LogRecord> records = rawStream
                .peek(line -> read.increment())
                .map(line -> parseOrNull(line, skipped))
                .filter(Objects::nonNull)
                .filter(filter)
                .peek(stats::accept)
                .iterator();

        return new ProcessingResult(records, stats, sources, read, skipped);
    }

    private @Nullable LogRecord parseOrNull(String line, LongAdder skipped) {
        try {
            return parser.parse(line);
        } catch (MalformedLineException e) {
            skipped.increment();
            LOG.warn("Пропущена некорректная строка: {}", e.getMessage());
            return null;
        }
    }
}
