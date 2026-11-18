package academy.incidentradar.source;

import java.io.Closeable;
import java.util.List;
import java.util.stream.Stream;

public interface LogSource extends Closeable {
    Stream<String> stream();

    List<String> sources();

    @Override
    void close();
}
