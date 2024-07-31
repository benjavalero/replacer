package es.bvalero.replacer.finder.replacement.finders;

import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.Suggestion;
import es.bvalero.replacer.finder.replacement.ReplacementFinder;
import es.bvalero.replacer.finder.util.FinderMatchResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
import java.util.List;
import java.util.regex.MatchResult;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Find character "ó" between numbers, e.g. `2 ó 3`
 */
@Component
class AcuteOFinder implements ReplacementFinder {

    static final String SEARCH_ACUTE_O = " ó ";
    static final String ACUTE_O = "ó";
    static final String FIX_ACUTE_O = "o";

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        if (WikipediaLanguage.SPANISH == page.getPageKey().getLang()) {
            return LinearMatchFinder.find(page, this::findAcuteO);
        } else {
            return List.of();
        }
    }

    @Nullable
    private MatchResult findAcuteO(FinderPage page, int start) {
        final String text = page.getContent();
        while (start >= 0 && start < text.length()) {
            final int startAcuteO = findStartAcuteO(text, start);
            if (startAcuteO < 0) {
                return null;
            }

            if (isImmutable(text, startAcuteO)) {
                return FinderMatchResult.of(startAcuteO, ACUTE_O);
            } else {
                start = startAcuteO + 2; // As we have searched with surrounding whitespaces
            }
        }
        return null;
    }

    private int findStartAcuteO(String text, int start) {
        final int startSearchAcuteO = text.indexOf(SEARCH_ACUTE_O, start);
        return startSearchAcuteO >= 0 ? startSearchAcuteO + 1 : -1;
    }

    private boolean isImmutable(String text, int startAcuteO) {
        // We need to check the words before and after
        // The char before and after the acute-o is a white-space, but we need to check the rest of chars in the middle.
        final MatchResult matchBefore = FinderUtils.findWordBefore(text, startAcuteO);
        if (matchBefore == null || (matchBefore.end() != startAcuteO - 1)) {
            return false;
        }

        final int endAcuteO = startAcuteO + ACUTE_O.length();
        final MatchResult matchAfter = FinderUtils.findWordAfter(text, endAcuteO);
        return matchAfter != null && (matchAfter.start() == endAcuteO + 1);
    }

    @Override
    public Replacement convert(MatchResult match, FinderPage page) {
        return Replacement.builder()
            .page(page)
            .type(StandardType.ACUTE_O)
            .start(match.start())
            .text(ACUTE_O)
            .suggestions(findSuggestions())
            .build();
    }

    private List<Suggestion> findSuggestions() {
        return List.of(Suggestion.ofNoComment(FIX_ACUTE_O));
    }
}
