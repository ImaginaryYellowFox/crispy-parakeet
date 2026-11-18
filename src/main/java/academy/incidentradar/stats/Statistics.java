package academy.incidentradar.stats;

import academy.incidentradar.model.LogRecord;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

public final class Statistics {

    private static final long EMPTY_STAT = 0L;
    private static final long INCREMENT = 1L;
    private static final double P95_PERCENTILE = 0.95;
    private static final double EMPTY_AVERAGE = 0.0;

    private static final Comparator<Map.Entry<String, Long>> TOP_RESOURCES_COMPARATOR =
            Map.Entry.<String, Long>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey());

    private long totalRequests;
    private final Map<Integer, Long> statusDistribution = new TreeMap<>();
    private final Map<String, Long> resourceCounts = new TreeMap<>();
    private final NavigableMap<Long, Long> bodySizeFrequency = new TreeMap<>();
    private long bodyBytesSum;
    private long bodyBytesMax = Long.MIN_VALUE;

    public void accept(LogRecord record) {
        totalRequests++;
        statusDistribution.merge(record.statusCode(), INCREMENT, Long::sum);
        resourceCounts.merge(record.resource(), INCREMENT, Long::sum);
        bodySizeFrequency.merge(record.bodyBytesSent(), INCREMENT, Long::sum);
        bodyBytesSum += record.bodyBytesSent();
        if (record.bodyBytesSent() > bodyBytesMax) {
            bodyBytesMax = record.bodyBytesSent();
        }
    }

    public long totalRequests() {
        return totalRequests;
    }

    public Map<Integer, Long> statusDistribution() {
        return Collections.unmodifiableMap(statusDistribution);
    }

    public Map<String, Long> topResources(int limit) {
        return resourceCounts.entrySet().stream()
                .sorted(TOP_RESOURCES_COMPARATOR)
                .limit(limit)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
    }

    public double averageBodyBytes() {
        return totalRequests == EMPTY_STAT ? EMPTY_AVERAGE : (double) bodyBytesSum / totalRequests;
    }

    public long maxBodyBytes() {
        return totalRequests == EMPTY_STAT ? EMPTY_STAT : bodyBytesMax;
    }

    public long p95BodyBytes() {
        if (totalRequests == EMPTY_STAT) {
            return EMPTY_STAT;
        }
        long rank = (long) Math.ceil(P95_PERCENTILE * totalRequests);
        if (rank < INCREMENT) {
            rank = INCREMENT;
        }
        long cumulative = 0;
        for (Map.Entry<Long, Long> e : bodySizeFrequency.entrySet()) {
            cumulative += e.getValue();
            if (cumulative >= rank) {
                return e.getKey();
            }
        }
        return bodyBytesMax;
    }
}
