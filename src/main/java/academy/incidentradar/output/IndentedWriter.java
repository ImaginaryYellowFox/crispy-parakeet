package academy.incidentradar.output;

import java.io.IOException;
import java.io.Writer;
import java.util.Objects;

public final class IndentedWriter {

    private final Writer writer;

    public IndentedWriter(Writer writer) {
        this.writer = Objects.requireNonNull(writer, "writer must not be null");
    }

    public void write(String s) throws IOException {
        writer.write(s);
    }

    public void writeln(String s) throws IOException {
        writer.write(s);
        writer.write('\n');
    }

    public void writeln() throws IOException {
        writer.write('\n');
    }
}
