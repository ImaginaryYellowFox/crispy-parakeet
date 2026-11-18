package academy.incidentradar.source;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public final class GlobFileResolver {
    private static final int START_INDEX = 0;
    private static final int NEXT_INDEX_OFFSET = 1;
    private static final String CURRENT_DIRECTORY = ".";

    private GlobFileResolver() {}

    public static List<Path> resolve(String pattern) {
        String dirStr = extractDirectory(pattern);
        String fileNamePattern = extractFileName(pattern);

        List<Path> matchedFiles = findMatchingFiles(dirStr, fileNamePattern, pattern);
        matchedFiles.sort(Path::compareTo);
        return matchedFiles;
    }

    private static String extractDirectory(String pattern) {
        int lastSep = pattern.lastIndexOf(File.separatorChar);
        return lastSep >= START_INDEX ? pattern.substring(START_INDEX, lastSep) : CURRENT_DIRECTORY;
    }

    private static String extractFileName(String pattern) {
        int lastSep = pattern.lastIndexOf(File.separatorChar);
        return lastSep >= START_INDEX ? pattern.substring(lastSep + NEXT_INDEX_OFFSET) : pattern;
    }

    private static List<Path> findMatchingFiles(String dirStr, String fileNamePattern, String fullPattern) {
        Path searchDir = Paths.get(dirStr);
        List<Path> matchedFiles = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(searchDir, fileNamePattern)) {
            for (Path entry : stream) {
                if (Files.isRegularFile(entry)) {
                    matchedFiles.add(entry);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось найти файлы по маске: " + fullPattern, e);
        }
        return matchedFiles;
    }
}
