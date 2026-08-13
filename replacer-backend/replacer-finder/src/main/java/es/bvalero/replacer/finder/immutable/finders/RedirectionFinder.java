package es.bvalero.replacer.finder.immutable.finders;

import com.roklenarcic.util.strings.StringMap;
import com.roklenarcic.util.strings.WholeWordMatchMap;
import es.bvalero.replacer.FinderProperties;
import es.bvalero.replacer.common.util.ReplacerUtils;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.FinderPriority;
import es.bvalero.replacer.finder.immutable.ImmutableFinder;
import es.bvalero.replacer.finder.util.FinderMatchResult;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.stream.Stream;

/**
 * Find redirection templates. In case they are found the complete text must be ignored.
 *
 * <p>Disabled: redirection pages are filtered upstream via {@code IndexablePage.isRedirect()}
 * (both in dump indexing and API review), so this finder would run on every non-redirection page
 * for no benefit. Kept for reference in case that upstream check ever becomes unreliable.
 */
// @Component — disabled, see Javadoc
public class RedirectionFinder implements ImmutableFinder {

    // Dependency injection
    private final FinderProperties finderProperties;

    private StringMap<String> stringMap;

    public RedirectionFinder(FinderProperties finderProperties) {
        this.finderProperties = finderProperties;
    }

    @PostConstruct
    public void init() {
        List<String> lowerCaseWords = this.finderProperties.getRedirectionWords()
            .stream()
            .map(ReplacerUtils::toLowerCase)
            .toList();
        char[] falseWordChars = { '-' };
        boolean[] wordCharFlags = { false };
        this.stringMap = new WholeWordMatchMap<>(lowerCaseWords, lowerCaseWords, true, falseWordChars, wordCharFlags);
    }

    @Override
    public FinderPriority getPriority() {
        // Redirections should be discarded using the information in the dump
        // This is run lastly just in case
        return FinderPriority.NONE;
    }

    @Override
    public Stream<MatchResult> findMatchResults(FinderPage page) {
        final String text = page.getContent();
        final String lowerCaseText = ReplacerUtils.toLowerCase(text);
        final RedirectionMatchListener listener = new RedirectionMatchListener();
        this.stringMap.match(lowerCaseText, listener);
        return listener.isFound() ? Stream.of(FinderMatchResult.of(0, text)) : Stream.of();
    }
}
