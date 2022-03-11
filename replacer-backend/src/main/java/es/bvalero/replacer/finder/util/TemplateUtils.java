package es.bvalero.replacer.finder.util;

import es.bvalero.replacer.common.domain.WikipediaPage;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import lombok.experimental.UtilityClass;
import org.springframework.lang.Nullable;

@UtilityClass
public class TemplateUtils {

    public static final String START_TEMPLATE = "{{";
    public static final String END_TEMPLATE = "}}";

    public List<LinearMatchResult> findAllTemplates(WikipediaPage page) {
        final List<LinearMatchResult> matches = new ArrayList<>(100);

        // Each template found may contain nested templates which are added after
        int start = 0;
        while (start >= 0 && start < page.getContent().length()) {
            // Use a LinkedList as some elements will be prepended
            final List<LinearMatchResult> subMatches = new LinkedList<>();
            start = findTemplate(page, start, subMatches);
            matches.addAll(subMatches);
        }

        return matches;
    }

    private int findTemplate(WikipediaPage page, int start, List<LinearMatchResult> matches) {
        final String text = page.getContent();
        final int startTemplate = findStartTemplate(text, start);
        if (startTemplate >= 0) {
            final LinearMatchResult completeMatch = findNestedTemplate(text, startTemplate, matches);
            if (completeMatch != null) {
                matches.add(0, completeMatch);
                return completeMatch.end();
            } else {
                // Template not closed. Not worth keep on searching as the next templates are considered as nested.
                if (!isFakeTemplate(text, startTemplate)) {
                    FinderUtils.logFinderResult(
                        page,
                        startTemplate,
                        startTemplate + START_TEMPLATE.length(),
                        "Template not closed"
                    );
                }
                return -1;
            }
        } else {
            return -1;
        }
    }

    private int findStartTemplate(String text, int start) {
        return text.indexOf(START_TEMPLATE, start);
    }

    private int findEndTemplate(String text, int start) {
        return text.indexOf(END_TEMPLATE, start);
    }

    private boolean isFakeTemplate(String text, int templateStart) {
        // There are some cases where curly braces inside a LaTeX formula may be confused with template start
        // We want to avoid the warning in these cases
        // This method is only called in case the template is not closed
        final char nextChar = text.charAt(templateStart + START_TEMPLATE.length());
        return !Character.isLetterOrDigit(nextChar);
    }

    /* Find the immutable of the template. It also finds nested templates and adds them to the given list. */
    @Nullable
    private LinearMatchResult findNestedTemplate(String text, int startTemplate, List<LinearMatchResult> matches) {
        final List<LinearMatchResult> nestedMatches = new ArrayList<>();
        int start = startTemplate;
        if (text.startsWith(START_TEMPLATE, start)) {
            start += START_TEMPLATE.length();
        }
        while (true) {
            final int end = findEndTemplate(text, start);
            if (end < 0) {
                return null;
            }

            final int startNested = findStartTemplate(text, start);
            if (startNested >= 0 && startNested < end) {
                // Nested
                // Find the end of the nested which can be the found end or forward in case of more nesting levels
                final LinearMatchResult nestedMatch = findNestedTemplate(text, startNested, matches);
                if (nestedMatch == null) {
                    return null;
                }

                matches.add(0, nestedMatch);
                nestedMatches.add(nestedMatch);

                // Prepare to find the next nested
                start = nestedMatch.end();
            } else {
                final LinearMatchResult completeMatch = LinearMatchResult.of(
                    startTemplate,
                    text.substring(startTemplate, end + END_TEMPLATE.length())
                );
                completeMatch.addGroups(nestedMatches);
                return completeMatch;
            }
        }
    }
}
