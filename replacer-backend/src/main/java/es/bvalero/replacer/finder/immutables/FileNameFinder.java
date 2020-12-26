package es.bvalero.replacer.finder.immutables;

import es.bvalero.replacer.finder.*;
import es.bvalero.replacer.page.IndexablePage;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import javax.annotation.Resource;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Find filenames, e.g. `xx.jpg` in `[[File:xx.jpg]]`
 */
@Component
public class FileNameFinder implements ImmutableFinder {

    private static final String START_FILE = "[[";

    // Files are also found in:
    // - Tag "gallery" ==> Managed in CompleteTagFinder
    // - Template "Gallery" ==> Managed in CompleteTemplateFinder
    // - Parameter values ==> Managed in TemplateParamFinder
    // - File template with additional parameters separated by pipes ==> Managed in LinkAliasedFinder

    @Resource
    private List<String> fileSpaces;

    @Override
    public int getMaxLength() {
        return 200;
    }

    @Override
    public Iterable<Immutable> find(IndexablePage page) {
        return new LinearIterable<>(page, this::findResult, this::convert);
    }

    @Nullable
    public MatchResult findResult(IndexablePage page, int start) {
        List<MatchResult> matches = new ArrayList<>(100);
        while (start >= 0 && start < page.getContent().length() && matches.isEmpty()) {
            start = findFileName(page.getContent(), start, matches);
        }
        return matches.isEmpty() ? null : matches.get(0);
    }

    private int findFileName(String text, int start, List<MatchResult> matches) {
        int startFile = findStartFile(text, start);
        if (startFile >= 0) {
            int startFileName = findStartFileName(text, startFile + START_FILE.length());
            if (startFileName >= 0) {
                int endFileName = findEndFile(text, startFileName);
                if (endFileName >= 0) {
                    matches.add(LinearMatcher.of(startFileName, text.substring(startFileName, endFileName)));
                    return endFileName;
                } else {
                    return startFileName;
                }
            } else {
                return startFile + START_FILE.length();
            }
        } else {
            return -1;
        }
    }

    private int findStartFile(String text, int start) {
        return text.indexOf(START_FILE, start);
    }

    private int findStartFileName(String text, int start) {
        StringBuilder prefixBuilder = new StringBuilder();
        for (int i = start; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == '|' || ch == ']') {
                // Not a file but a hyperlink
                return -1;
            } else if (ch == ':') {
                String prefix = FinderUtils.setFirstUpperCase(prefixBuilder.toString());
                return fileSpaces.contains(prefix) && (i + 1 < text.length()) ? i + 1 : -1;
            } else {
                prefixBuilder.append(ch);
            }
        }
        return -1;
    }

    private int findEndFile(String text, int start) {
        for (int i = start; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == ']') {
                return i;
            } else if (ch == '|') {
                return -1;
            }
        }
        return -1;
    }
}
