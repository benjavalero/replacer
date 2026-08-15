package es.bvalero.replacer.finder.immutable.finders;

import es.bvalero.replacer.FinderProperties;
import es.bvalero.replacer.common.util.ReplacerUtils;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.FinderPriority;
import es.bvalero.replacer.finder.immutable.ImmutableFinder;
import es.bvalero.replacer.finder.util.FinderMatchRange;
import es.bvalero.replacer.finder.util.FinderUtils;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;

/**
 * Find redirection templates. In case they are found the complete text must be ignored.
 *
 * <p>Finds the '#' character and checks if the following characters (up to the length of the longest
 * redirection word) match any configured redirection word, comparing in lowercase without allocating
 * a full lowercase copy of the text.
 */
@Component
public class RedirectionFinder implements ImmutableFinder {

    // Dependency injection
    private final FinderProperties finderProperties;

    private List<String> redirectionWords;
    private int maxWordLength;

    public RedirectionFinder(FinderProperties finderProperties) {
        this.finderProperties = finderProperties;
    }

    @PostConstruct
    public void init() {
        this.redirectionWords = new ArrayList<>();
        this.finderProperties.getRedirectionWords()
            .stream()
            .map(ReplacerUtils::toLowerCase)
            .forEach(this.redirectionWords::add);
        this.maxWordLength = this.redirectionWords.stream().mapToInt(String::length).max().orElse(0);
    }

    @Override
    public FinderPriority getPriority() {
        return FinderPriority.HIGH;
    }

    @Override
    public Stream<MatchResult> findMatchResults(FinderPage page) {
        final String text = page.getContent();
        int start = 0;
        while (start < text.length()) {
            final int pos = text.indexOf('#', start);
            if (pos < 0) break;
            final int end = Math.min(pos + maxWordLength, text.length());
            final String candidate = ReplacerUtils.toLowerCase(text.substring(pos, end));
            for (String word : redirectionWords) {
                if (candidate.startsWith(word) && FinderUtils.isWordCompleteInText(pos, word, text)) {
                    return Stream.of(FinderMatchRange.of(text, 0, text));
                }
            }
            start = pos + 1;
        }
        return Stream.of();
    }
}
