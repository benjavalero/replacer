package es.bvalero.replacer.finder.replacement.finders;

import es.bvalero.replacer.common.domain.Replacement;
import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.Suggestion;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.finder.replacement.ReplacementFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
import es.bvalero.replacer.finder.util.LinearMatchResult;
import java.util.Collections;
import java.util.List;
import java.util.regex.MatchResult;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Find character "ó" between numbers, e.g. `2 ó 3`
 */
@Component
public class AcuteOFinder implements ReplacementFinder {

    static final String SEARCH_ACUTE_O = " ó ";
    static final String ACUTE_O = "ó";
    static final String FIX_ACUTE_O = "o";

    @Override
    public Iterable<MatchResult> findMatchResults(WikipediaPage page) {
        if (WikipediaLanguage.SPANISH == page.getId().getLang()) {
            return LinearMatchFinder.find(page, this::findAcuteO);
        } else {
            return Collections.emptyList();
        }
    }

    @Nullable
    private MatchResult findAcuteO(WikipediaPage page, int start) {
        final String text = page.getContent();
        while (start >= 0 && start < text.length()) {
            final int startAcuteO = findStartAcuteO(text, start);
            if (startAcuteO >= 0) {
                final int endAcuteO = startAcuteO + ACUTE_O.length();
                final LinearMatchResult match = findMatch(text, startAcuteO, endAcuteO);
                if (match != null) {
                    return match;
                } else {
                    start = endAcuteO;
                }
            } else {
                return null;
            }
        }
        return null;
    }

    private int findStartAcuteO(String text, int start) {
        final int startSearchAcuteO = text.indexOf(SEARCH_ACUTE_O, start);
        return startSearchAcuteO >= 0 ? startSearchAcuteO + 1 : -1;
    }

    @Nullable
    private LinearMatchResult findMatch(String text, int startAcuteO, int endAcuteO) {
        final LinearMatchResult wordBefore = findWordBefore(text, startAcuteO);
        final LinearMatchResult wordAfter = findWordAfter(text, endAcuteO);
        if (wordBefore != null && wordAfter != null) {
            return LinearMatchResult.of(startAcuteO, ACUTE_O);
        } else {
            return null;
        }
    }

    @Nullable
    private LinearMatchResult findWordBefore(String text, int startAcuteO) {
        final LinearMatchResult matchBefore = FinderUtils.findWordBefore(text, startAcuteO);
        // The char before the acute-o is a white-space, but we need to check the rest of chars in the middle.
        if (matchBefore == null || (matchBefore.end() != startAcuteO - 1)) {
            return null;
        } else {
            return matchBefore;
        }
    }

    @Nullable
    private LinearMatchResult findWordAfter(String text, int endAcuteO) {
        final LinearMatchResult matchAfter = FinderUtils.findWordAfter(text, endAcuteO);
        // The char after the acute-o is a white-space, but we need to check the rest of chars in the middle.
        if (matchAfter == null || (matchAfter.start() != endAcuteO + 1)) {
            return null;
        } else {
            return matchAfter;
        }
    }

    @Override
    public Replacement convert(MatchResult match, WikipediaPage page) {
        return Replacement
            .builder()
            .type(ReplacementType.ACUTE_O)
            .start(match.start())
            .text(ACUTE_O)
            .suggestions(findSuggestions())
            .build();
    }

    private List<Suggestion> findSuggestions() {
        return Collections.singletonList(Suggestion.ofNoComment(FIX_ACUTE_O));
    }
}
