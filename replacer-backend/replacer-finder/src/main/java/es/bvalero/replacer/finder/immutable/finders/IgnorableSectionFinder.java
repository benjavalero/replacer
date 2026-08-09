package es.bvalero.replacer.finder.immutable.finders;

import static es.bvalero.replacer.finder.util.FinderUtils.NEW_LINE;

import es.bvalero.replacer.FinderProperties;
import es.bvalero.replacer.common.util.ReplacerUtils;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.FinderPriority;
import es.bvalero.replacer.finder.immutable.ImmutableFinder;
import es.bvalero.replacer.finder.util.FinderMatchRange;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
import jakarta.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

/** Find ignorable sections in a page content */
@Component
public class IgnorableSectionFinder implements ImmutableFinder {

    private static final char HEADER_CHAR = '=';
    private static final String START_HEADER = "==";

    // Dependency injection
    private final FinderProperties finderProperties;

    private final Set<String> ignorableSections = new HashSet<>();

    public IgnorableSectionFinder(FinderProperties finderProperties) {
        this.finderProperties = finderProperties;
    }

    @PostConstruct
    public void init() {
        this.ignorableSections.addAll(this.finderProperties.getIgnorableSections()); // Cache
    }

    @Override
    public FinderPriority getPriority() {
        return FinderPriority.HIGH;
    }

    @Override
    public Stream<MatchResult> findMatchResults(FinderPage page) {
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

            if (!validateHeader(text, startHeader, endHeader)) {
                // Not ignorable section
                start = endHeader;
                continue;
            }

            final int endSection = findStartNextHeader(text, startHeader, endHeader);
            return FinderMatchRange.of(text, startHeader, endSection);
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

    // Strip the header chars at start and end of the header, and trim the result,
    // and check if the label is in the ignorable sections list.
    private boolean validateHeader(String text, int startHeader, int endHeader) {
        // We can assume the first two characters are the start of a header
        int labelStart = findHeaderLevelEnd(text, startHeader);
        while (labelStart < endHeader && Character.isWhitespace(text.charAt(labelStart))) {
            labelStart++;
        }
        int labelEnd = endHeader;
        while (labelEnd > labelStart && text.charAt(labelEnd - 1) == HEADER_CHAR) {
            labelEnd--;
        }
        labelEnd = FinderUtils.trimRight(text, labelStart, labelEnd);
        final int labelFinalStart = labelStart; // Lambda requires effectively final variable
        return this.ignorableSections.stream().anyMatch(s ->
            ReplacerUtils.containsAtPosition(text, s, labelFinalStart)
        );
    }

    private int findStartNextHeader(String text, int startHeader, int endHeader) {
        final int headerLevelEnd = findHeaderLevelEnd(text, startHeader);
        final int headerLevelLength = headerLevelEnd - startHeader;
        assert headerLevelLength >= 2; // At least "=="

        int startNextHeader = findStartHeader(text, endHeader);
        while (startNextHeader >= 0 && startNextHeader < text.length()) {
            final int nextHeaderLevelEnd = findHeaderLevelEnd(text, startNextHeader);
            final int nextHeaderLevelLength = nextHeaderLevelEnd - startNextHeader;
            if (nextHeaderLevelLength > headerLevelLength) {
                // Lower level, so we need to find the next header of the same level or higher
                final int endNextHeader = findEndHeader(text, startNextHeader);
                startNextHeader = findStartHeader(text, endNextHeader);
            } else {
                return startNextHeader;
            }
        }
        return text.length();
    }

    private int findHeaderLevelEnd(String text, int startHeader) {
        assert text.charAt(startHeader) == HEADER_CHAR && text.charAt(startHeader + 1) == HEADER_CHAR;
        for (int i = startHeader + START_HEADER.length(); i < text.length(); i++) {
            if (text.charAt(i) != HEADER_CHAR) {
                return i;
            }
        }
        return text.length();
    }
}
