package es.bvalero.replacer.finder.listing;

import java.io.BufferedReader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

public class ListingSorter {

    private static final String HEADER_TAG = "==";

    @SneakyThrows
    public static void main(String[] args) {
        try (
            Stream<Path> paths = Files.list(Paths.get("replacer-finder/src/main/resources/offline"))
                .filter(Files::isRegularFile)
                .filter(path -> path.getFileName().toString().startsWith("misspelling-list-"))
                .filter(path -> path.toString().endsWith(".txt"))
        ) {
            paths.forEach(ListingSorter::sortFile);
        }
    }

    @SneakyThrows
    private static void sortFile(Path filePath) {
        String content = Files.readString(filePath);
        List<String> lines = new BufferedReader(new StringReader(content)).lines().toList();

        List<String> result = new ArrayList<>();
        List<String> section = new ArrayList<>();
        boolean inHeader = false;

        // Process each line: keep initial lines as-is, then sort content between headers
        for (String line : lines) {
            if (isHeader(line)) {
                // When we find a new header, sort and add the previous section (if any)
                if (inHeader) {
                    result.addAll(sortSection(section));
                    section.clear();
                }
                result.add(line);
                inHeader = true;
            } else if (inHeader) {
                // Collect lines belonging to the current section
                section.add(line);
            } else {
                // Keep lines before the first header as-is
                result.add(line);
            }
        }

        // Sort and add the last section if it exists
        if (inHeader) {
            result.addAll(sortSection(section));
        }

        String updatedContent = String.join("\n", result);
        Files.writeString(filePath, updatedContent);
        System.out.println("Processed: " + filePath.getFileName());
    }

    private static List<String> sortSection(List<String> section) {
        if (section.isEmpty()) {
            return List.of();
        }
        TreeMap<String, String> sortedMap = new TreeMap<>(new ListingComparator());
        section
            .stream()
            .filter(ListingSorter::isListingLine)
            .map(String::stripTrailing)
            .forEach(line -> sortedMap.put(getListingKey(line), line));

        // Add blank line after sorted section
        return Stream.concat(sortedMap.values().stream(), Stream.of(StringUtils.EMPTY)).toList();
    }

    private static boolean isHeader(String line) {
        if (!line.startsWith(HEADER_TAG) || !line.endsWith(HEADER_TAG)) {
            return false;
        }
        String content = line.substring(HEADER_TAG.length(), line.length() - HEADER_TAG.length()).trim();
        return isUppercaseLetter(content) || content.equals("0-9") || content.equals("!$@");
    }

    private static boolean isUppercaseLetter(String content) {
        return content.length() == 1 && Character.isUpperCase(content.charAt(0));
    }

    private static boolean isListingLine(String line) {
        if (StringUtils.isBlank(line) || !line.startsWith(" ")) {
            return false;
        }
        String[] tokens = line.split("\\|", -1);
        return tokens.length == 3 && StringUtils.isNotBlank(tokens[0]);
    }

    private static String getListingKey(String line) {
        return line.split("\\|", -1)[0].trim();
    }
}
