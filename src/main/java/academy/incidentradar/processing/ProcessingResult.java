package academy.incidentradar.processing;

import academy.incidentradar.model.LogRecord;
import academy.incidentradar.stats.Statistics;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.LongAdder;

public final class ProcessingResult {

    private final Iterator<LogRecord> records;
    private final Statistics statistics;
    private final List<String> sources;
    private final LongAdder linesRead;
    private final LongAdder linesSkipped;

    public ProcessingResult(
            Iterator<LogRecord> records,
            Statistics statistics,
            List<String> sources,
            LongAdder linesRead,
            LongAdder linesSkipped) {
        this.records = Objects.requireNonNull(records, "records must not be null");
        this.statistics = Objects.requireNonNull(statistics, "statistics must not be null");
        this.sources = List.copyOf(sources);
        this.linesRead = linesRead;
        this.linesSkipped = linesSkipped;
    }

    public Iterator<LogRecord> records() {
        return records;
    }

    public Statistics statistics() {
        return statistics;
    }

    public List<String> sources() {
        return sources;
    }

    public long linesRead() {
        return linesRead.sum();
    }

    public long linesSkipped() {
        return linesSkipped.sum();
    }

    public Statistics drain() {
        while (records.hasNext()) {
            records.next();
        }
        return statistics;
    }
}
