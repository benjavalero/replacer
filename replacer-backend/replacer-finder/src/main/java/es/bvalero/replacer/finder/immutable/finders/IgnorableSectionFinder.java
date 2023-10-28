package es.bvalero.replacer.finder.immutable.finders;

import static es.bvalero.replacer.finder.util.FinderUtils.NEW_LINE;

import es.bvalero.replacer.FinderProperties;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.FinderPriority;
import es.bvalero.replacer.finder.immutable.ImmutableFinder;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
import es.bvalero.replacer.finder.util.LinearMatchResult;
import java.util.regex.MatchResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/** Find ignorable sections in a page content */
@Component
class IgnorableSectionFinder implements ImmutableFinder {

    private static final char HEADER_CHAR = '=';
    private static final String START_HEADER = "==";

    @Autowired
    private FinderProperties finderProperties;

    @Override
    public FinderPriority getPriority() {
        return FinderPriority.HIGH;
    }

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        return LinearMatchFinder.find(page, this::findSection);
    }

    @Nullable
    private MatchResult findSection(FinderPage page, int start) {
        final String text = page.getContent();
        while (start >= 0 && start < text.length()) {
            final int startHeader = findStartHeader(text, start);
            if (startHeader < 0) {
                return null;
            }

            final int endHeader = findEndHeader(text, startHeader);
            if (endHeader < 0) {
                // No new line found to end the header, so we are at the end of the text.
                return null;
            }

            // We can assume the first two characters are the start of a header
            // There is no significant improvement by using "strip" method instead
            final String header = text.substring(startHeader + START_HEADER.length(), endHeader);
            final String label = StringUtils.remove(header, HEADER_CHAR).trim();
            if (!isValidHeaderLabel(label)) {
                // Not ignorable section
                start = endHeader;
                continue;
            }

            final int endSection = findNextHeader(text, endHeader, findHeaderLevel(text, startHeader));
            return LinearMatchResult.of(text, startHeader, endSection);
        }
        return null;
    }

    private int findStartHeader(String text, int start) {
        return text.indexOf(START_HEADER, start);
    }

    private int findEndHeader(String text, int start) {
        // We can assume the first two characters are the start of a header
        return text.indexOf(NEW_LINE, start + START_HEADER.length());
    }

    private String findHeaderLevel(String text, int start) {
        for (int i = start; i < text.length(); i++) {
            if (text.charAt(i) != '=') {
                return text.substring(start, i);
            }
        }
        throw new IllegalStateException();
    }

    private int findNextHeader(String text, int start, String headerLevel) {
        final int pos = text.indexOf(headerLevel, start);
        if (pos >= 0) {
            final int endHeader = pos + headerLevel.length();
            // In case we find a subsection, find again.
            if (endHeader < text.length() && text.charAt(endHeader) == HEADER_CHAR) {
                // As this is a corner-case, there is no performance penalty on using recursion here.
                return findNextHeader(text, endHeader + 1, headerLevel);
            } else {
                return pos;
            }
        } else {
            return text.length();
        }
    }

    private boolean isValidHeaderLabel(String label) {
        return this.finderProperties.getIgnorableSections().stream().anyMatch(label::startsWith);
    }
}
