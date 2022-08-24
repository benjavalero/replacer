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
        final int startAcuteO = text.indexOf(SEARCH_ACUTE_O, start);
        if (startAcuteO >= 0) {
            final int endAcuteO = startAcuteO + SEARCH_ACUTE_O.length();
            final String wordBefore = findWordBefore(text, startAcuteO);
            final String wordAfter = findWordAfter(text, endAcuteO);
            if (wordBefore == null || wordAfter == null) {
                return endAcuteO;
            } else {
                matches.add(
                    LinearMatchResult.of(startAcuteO - wordBefore.length(), wordBefore + SEARCH_ACUTE_O + wordAfter)
                );
                return startAcuteO + SEARCH_ACUTE_O.length();
            }
        } else {
            return -1;
        }
    }

    @Override
    public Replacement convert(MatchResult match, WikipediaPage page) {
        return Replacement
            .builder()
            .type(findReplacementType(match.group()))
            .start(match.start() + match.group().indexOf(SEARCH_ACUTE_O) + 1)
            .text(ACUTE_O)
            .suggestions(findSuggestions())
            .build();
    }

    private ReplacementType findReplacementType(String text) {
        final int pos = text.indexOf(SEARCH_ACUTE_O);
        return (
                FinderUtils.isNumber(text.substring(0, pos)) &&
                FinderUtils.isNumber(text.substring(pos + SEARCH_ACUTE_O.length()))
            )
            ? ReplacementType.ACUTE_O_NUMBERS
            : ReplacementType.ACUTE_O_WORDS;
    }

    private List<Suggestion> findSuggestions() {
        return Collections.singletonList(Suggestion.ofNoComment(FIX_ACUTE_O));
    }

    @Nullable
    private String findWordAfter(String text, int start) {
        int end;
        for (end = start; end < text.length(); end++) {
            final char ch = text.charAt(end);
            if (!Character.isLetterOrDigit(ch)) {
                break;
            }
        }
        final String word = text.substring(start, end);
        return StringUtils.isBlank(word) ? null : word.trim();
    }

    @Nullable
    private String findWordBefore(String text, int start) {
        int end;
        for (end = start - 1; end >= 0; end--) {
            final char ch = text.charAt(end);
            if (!Character.isLetterOrDigit(ch)) {
                break;
            }
        }
        final String word = text.substring(Math.max(0, end), start);
        return StringUtils.isBlank(word) ? null : word.trim();
    }
}
