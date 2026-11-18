package academy.incidentradar.source;

import academy.incidentradar.error.SourceException;
import academy.incidentradar.validation.ConfigValidator;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;

public final class FileLogSource implements LogSource {

    private final String path;

    @Nullable
    private List<Path> resolvedGlobCache;

    public FileLogSource(String path) {
        this.path = path;
    }

    @Override
    public Stream<String> stream() {
        if (isGlob()) {
            return resolveGlob().stream().flatMap(this::openFileStreamFromPath);
        }
        return openFileStream(path);
    }

    @Override
    public List<String> sources() {
        if (isGlob()) {
            return resolveGlob().stream().map(Path::toString).toList();
        }
        return List.of(path);
    }

    @Override
    public void close() {}

    private boolean isGlob() {
        return ConfigValidator.isGlobPattern(path);
    }

    private List<Path> resolveGlob() {
        if (resolvedGlobCache == null) {
            resolvedGlobCache = GlobFileResolver.resolve(path);
        }
        return resolvedGlobCache;
    }

    @SuppressWarnings("StreamResourceLeak")
    private Stream<String> openFileStream(String filePath) {
        Path p = Paths.get(filePath);
        if (!p.toFile().exists()) {
            throw new SourceException("Файл не найден: " + filePath);
        }
        return openFileStreamFromPath(p);
    }

    @SuppressWarnings("StreamResourceLeak")
    private Stream<String> openFileStreamFromPath(Path file) {
        try {
            return Files.lines(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new SourceException("Не удалось прочитать файл: " + file, e);
        }
    }
}
