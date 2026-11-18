package academy.incidentradar.output;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class MarkdownRenderer {

    private static final int MIN_HEADING_LEVEL = 1;
    private static final int MAX_HEADING_LEVEL = 6;

    private final IndentedWriter w;

    public MarkdownRenderer(IndentedWriter w) {
        this.w = w;
    }

    public void renderHeading(int level, String title) throws IOException {
        if (level < MIN_HEADING_LEVEL || level > MAX_HEADING_LEVEL) {
            throw new IllegalArgumentException(
                    "Уровень заголовка должен быть от " + MIN_HEADING_LEVEL + " до " + MAX_HEADING_LEVEL);
        }
        w.writeln("#".repeat(level) + " " + title);
        w.writeln();
    }

    public void renderTable(List<String> headers, List<List<String>> rows) throws IOException {
        w.writeln("| " + String.join(" | ", headers) + " |");
        w.writeln("|" + headers.stream().map(h -> "---").collect(Collectors.joining("|")) + "|");

        for (List<String> row : rows) {
            w.writeln("| " + String.join(" | ", row) + " |");
        }

        w.writeln();
    }

    public void renderKeyValueTable(Map<String, String> data) throws IOException {
        List<List<String>> rows = data.entrySet().stream()
                .map(e -> List.of(e.getKey(), e.getValue()))
                .toList();
        renderTable(List.of("Parameter", "Value"), rows);
    }

    public void renderLogLines(List<String> lines) throws IOException {
        for (String line : lines) {
            w.writeln("- `%s`".formatted(line));
        }
        w.writeln();
    }

    public void renderSources(List<String> sources) throws IOException {
        if (sources.isEmpty()) {
            w.writeln("_Нет источников_");
            w.writeln();
            return;
        }

        List<List<String>> rows = new java.util.ArrayList<>();
        for (int i = 0; i < sources.size(); i++) {
            rows.add(List.of(String.valueOf(i + 1), "`" + sources.get(i) + "`"));
        }
        renderTable(List.of("#", "Source"), rows);
    }

    public void renderDistributionTable(Map<?, Long> data, String col1Name, String col2Name) throws IOException {
        List<List<String>> rows = data.entrySet().stream()
                .map(e -> List.of(String.valueOf(e.getKey()), String.valueOf(e.getValue())))
                .toList();
        renderTable(List.of(col1Name, col2Name), rows);
    }
}
