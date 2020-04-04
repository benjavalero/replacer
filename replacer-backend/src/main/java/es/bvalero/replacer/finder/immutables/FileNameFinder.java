package es.bvalero.replacer.finder.immutables;

import es.bvalero.replacer.finder.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.MatchResult;
import org.springframework.stereotype.Component;

/**
 * Find filenames, e. g. `xx.jpg` in `[[File:xx.jpg]]`
 */
@Component
public class FileNameFinder implements ImmutableFinder {
    // Files are also found in:
    // - Tag "gallery" ==> Managed in CompleteTagFinder
    // - Template "Gallery" ==> Managed in CompleteTemplateFinder
    // - Parameter values without File prefix ==> To be managed in ParameterValueFinder

    private static final List<String> ALLOWED_PREFIXES = Arrays.asList("Archivo", "File", "Imagen", "Image");

    @Override
    public ImmutableFinderPriority getPriority() {
        return ImmutableFinderPriority.MEDIUM;
    }

    @Override
    public int getMaxLength() {
        return 150;
    }

    @Override
    public Iterable<Immutable> find(String text) {
        return new LinearIterable<>(text, this::findResult, this::convert);
    }

    public MatchResult findResult(String text, int start) {
        List<MatchResult> matches = new ArrayList<>(100);
        while (start >= 0 && matches.isEmpty()) {
            start = findFileName(text, start, matches);
        }
        return matches.isEmpty() ? null : matches.get(0);
    }

    private int findFileName(String text, int start, List<MatchResult> matches) {
        int startFile = findStartFile(text, start);
        if (startFile >= 0) {
            int startFileName = findStartFileName(text, startFile + 2);
            if (startFileName >= 0) {
                int endFileName = findEndFile(text, startFileName);
                if (endFileName >= 0) {
                    matches.add(LinearMatcher.of(startFileName, text.substring(startFileName, endFileName)));
                    return endFileName;
                } else {
                    return startFileName;
                }
            } else {
                return startFile + 2;
            }
        } else {
            return -1;
        }
    }

    private int findStartFile(String text, int start) {
        return text.indexOf("[[", start);
    }

    private int findStartFileName(String text, int start) {
        StringBuilder prefixBuilder = new StringBuilder();
        for (int i = start; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == '|' || ch == ']') {
                // Not a file but a hyperlink
                return -1;
            } else if (ch == ':') {
                String prefix = prefixBuilder.toString();
                return ALLOWED_PREFIXES.contains(prefix) && (i + 1 < text.length()) ? i + 1 : -1;
            } else {
                prefixBuilder.append(ch);
            }
        }
        return -1;
    }

    private int findEndFile(String text, int start) {
        for (int i = start; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == '|' || ch == ']') {
                return i;
            }
        }
        return -1;
    }
}
