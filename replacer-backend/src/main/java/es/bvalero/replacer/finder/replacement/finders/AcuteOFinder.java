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
import org.apache.commons.lang3.StringUtils;
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

    private int findAcuteO(WikipediaPage page, int start, List<MatchResult> matches) {
        final String text = page.getContent();
        final int startAcuteO = findStartAcuteO(text, start);
        if (startAcuteO >= 0) {
            final int endAcuteO = startAcuteO + ACUTE_O.length();
            final LinearMatchResult match = findMatch(text, startAcuteO, endAcuteO);
            if (match != null) {
                matches.add(match);
            }
            return endAcuteO;
        } else {
            return -1;
        }
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
            final LinearMatchResult match = LinearMatchResult.of(startAcuteO, ACUTE_O);
            final List<LinearMatchResult> groups = List.of(wordBefore, wordAfter);
            match.addGroups(groups);
            return match;
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
            .type(findReplacementType((LinearMatchResult) match))
            .start(match.start())
            .text(ACUTE_O)
            .suggestions(findSuggestions())
            .build();
    }

    private ReplacementType findReplacementType(LinearMatchResult match) {
        return (StringUtils.isNumeric(match.group(0)) && StringUtils.isNumeric(match.group(1)))
            ? ReplacementType.ACUTE_O_NUMBERS
            : ReplacementType.ACUTE_O_WORDS;
    }

    private List<Suggestion> findSuggestions() {
        return Collections.singletonList(Suggestion.ofNoComment(FIX_ACUTE_O));
    }
}
