package es.bvalero.replacer.finder.immutable.finders;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.FinderPriority;
import es.bvalero.replacer.finder.immutable.ImmutableCheckedFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.LinearMatchResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/** Find words in the page title */
@Component
class TitleFinder extends ImmutableCheckedFinder {

    private static final int MIN_WORD_LENGTH = 4;

    @Override
    public FinderPriority getPriority() {
        return FinderPriority.HIGH;
    }

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        // With few title words, the performance of the simple index-of and the Aho-Corasick is similar.
        final List<MatchResult> matches = new ArrayList<>(100);
        final String text = page.getContent();
        final Set<String> words = findTitleWords(page.getTitle());
        for (String word : words) {
            // Find the word case-sensitive improves the performance
            int start = 0;
            while (start >= 0 && start < text.length()) {
                final int wordStart = text.indexOf(word, start);
                if (wordStart >= 0) {
                    final LinearMatchResult match = LinearMatchResult.of(wordStart, word);
                    matches.add(match);
                    start = match.end();
                } else {
                    break;
                }
            }
        }
        return matches;
    }

    private Set<String> findTitleWords(String title) {
        return FinderUtils
            .findAllWords(title)
            .stream()
            .map(MatchResult::group)
            .filter(this::isTitleWordImmutable)
            .collect(Collectors.toUnmodifiableSet());
    }

    private boolean isTitleWordImmutable(String word) {
        return word.length() >= MIN_WORD_LENGTH;
    }

    @Override
    public boolean validate(MatchResult match, FinderPage page) {
        return FinderUtils.isWordCompleteInText(match.start(), match.group(), page.getContent());
    }
}
