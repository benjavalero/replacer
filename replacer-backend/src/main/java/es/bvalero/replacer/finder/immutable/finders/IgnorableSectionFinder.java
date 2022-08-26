package es.bvalero.replacer.finder.immutable.finders;

import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.finder.FinderPriority;
import es.bvalero.replacer.finder.immutable.ImmutableFinder;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
import es.bvalero.replacer.finder.util.LinearMatchResult;
import java.util.List;
import java.util.Set;
import java.util.regex.MatchResult;
import javax.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/** Find ignorable sections in a page content */
@Component
class IgnorableSectionFinder implements ImmutableFinder {

    private static final char HEADER_CHAR = '=';
    private static final String START_HEADER = "==";
    private static final char NEW_LINE = '\n';

    @Resource
    private Set<String> ignorableSections;

    @Override
    public FinderPriority getPriority() {
        return FinderPriority.HIGH;
    }

    @Override
    public Iterable<MatchResult> findMatchResults(WikipediaPage page) {
        return LinearMatchFinder.find(page, this::findSection);
    }

    private int findSection(WikipediaPage page, int start, List<MatchResult> matches) {
        final String text = page.getContent();
        final int startHeader = findStartHeader(text, start);
        if (startHeader >= 0) {
            final int endHeader = findEndHeader(text, startHeader);
            if (endHeader >= 0) {
                final String header = text.substring(startHeader, endHeader);
                final String label = StringUtils.remove(header, HEADER_CHAR).trim();
                if (isValidHeaderLabel(label)) {
                    final int startNextHeader = findStartHeader(text, endHeader);
                    final int endSection = startNextHeader >= 0 ? startNextHeader : text.length();
                    matches.add(LinearMatchResult.of(startHeader, text.substring(startHeader, endSection)));
                    return endSection;
                } else {
                    // Not ignorable section
                    return endHeader;
                }
            } else {
                // No new line found to end the header, so we are at the end of the text.
                return -1;
            }
        } else {
            return -1;
        }
    }

    private int findStartHeader(String text, int start) {
        return text.indexOf(START_HEADER, start);
    }

    private int findEndHeader(String text, int start) {
        return text.indexOf(NEW_LINE, start);
    }

    private boolean isValidHeaderLabel(String label) {
        return ignorableSections.contains(label);
    }
}
