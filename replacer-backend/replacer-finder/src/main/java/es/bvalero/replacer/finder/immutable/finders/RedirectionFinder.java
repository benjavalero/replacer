package es.bvalero.replacer.finder.immutable.finders;

import es.bvalero.replacer.FinderProperties;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.FinderPriority;
import es.bvalero.replacer.finder.immutable.ImmutableFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.LinearMatchResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.MatchResult;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Find redirection templates. In case they are found the complete text must be ignored. */
@Component
class RedirectionFinder implements ImmutableFinder {

    @Autowired
    private FinderProperties finderProperties;

    private final List<String> redirectionWords = new ArrayList<>();

    @Override
    public FinderPriority getPriority() {
        // Redirections should be discarded using the information in the dump
        // This is run lastly just in case
        return FinderPriority.NONE;
    }

    @PostConstruct
    public void init() {
        this.redirectionWords.addAll(
                this.finderProperties.getRedirectionWords().stream().map(FinderUtils::toLowerCase).toList()
            );
    }

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        // With only 3 redirection words, the performance of the simple index-of and the Aho-Corasick is similar.
        final String text = page.getContent();
        final String lowerCaseText = FinderUtils.toLowerCase(text);
        for (String redirectionWord : this.redirectionWords) {
            if (lowerCaseText.contains(redirectionWord)) {
                return Set.of(LinearMatchResult.of(0, text));
            }
        }
        return Set.of();
    }
}
