package es.bvalero.replacer.finder.util;

import es.bvalero.replacer.finder.FinderPage;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TemplateUtils {

    public static final String START_TEMPLATE = "{{";
    public static final String END_TEMPLATE = "}}";

    public static List<LinearMatchResult> findAllTemplates(FinderPage page) {
        List<LinearMatchResult> matches = new ArrayList<>(100);

        // Each template found may contain nested templates which are added after
        int start = 0;
        while (start >= 0 && start < page.getContent().length()) {
            // Use a LinkedList as some elements will be prepended
            List<LinearMatchResult> subMatches = new LinkedList<>();
            start = findTemplate(page, start, subMatches);
            matches.addAll(subMatches);
        }

        return matches;
    }

    private static int findTemplate(FinderPage page, int start, List<LinearMatchResult> matches) {
        String text = page.getContent();
        int startTemplate = findStartTemplate(text, start);
        if (startTemplate >= 0) {
            LinearMatchResult completeMatch = findNestedTemplate(text, startTemplate, matches);
            if (completeMatch != null) {
                matches.add(0, completeMatch);
                return completeMatch.end();
            } else {
                // Template not closed. Not worth keep on searching as the next templates are considered as nested.
                FinderUtils.logWarning(
                    text,
                    startTemplate,
                    startTemplate + START_TEMPLATE.length(),
                    page,
                    "Template not closed"
                );
                return -1;
            }
        } else {
            return -1;
        }
    }

    private static int findStartTemplate(String text, int start) {
        return text.indexOf(START_TEMPLATE, start);
    }

    private static int findEndTemplate(String text, int start) {
        return text.indexOf(END_TEMPLATE, start);
    }

    /* Find the immutable of the template. It also finds nested templates and adds them to the given list. */
    @Nullable
    private static LinearMatchResult findNestedTemplate(
        String text,
        int startTemplate,
        List<LinearMatchResult> matches
    ) {
        List<LinearMatchResult> nestedMatches = new ArrayList<>();
        int start = startTemplate;
        if (text.startsWith(START_TEMPLATE, start)) {
            start += START_TEMPLATE.length();
        }
        while (true) {
            int end = findEndTemplate(text, start);
            if (end < 0) {
                return null;
            }

            int startNested = findStartTemplate(text, start);
            if (startNested >= 0 && startNested < end) {
                // Nested
                // Find the end of the nested which can be the found end or forward in case of more nesting levels
                LinearMatchResult nestedMatch = findNestedTemplate(text, startNested, matches);
                if (nestedMatch == null) {
                    return null;
                }

                matches.add(0, nestedMatch);
                nestedMatches.add(nestedMatch);

                // Prepare to find the next nested
                start = nestedMatch.end();
            } else {
                LinearMatchResult completeMatch = LinearMatchResult.of(
                    startTemplate,
                    text.substring(startTemplate, end + END_TEMPLATE.length())
                );
                completeMatch.addGroups(nestedMatches);
                return completeMatch;
            }
        }
    }
}
