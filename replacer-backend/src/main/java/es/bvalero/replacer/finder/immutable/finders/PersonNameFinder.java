package es.bvalero.replacer.finder.immutable.finders;

import es.bvalero.replacer.common.domain.Immutable;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.finder.FinderPriority;
import es.bvalero.replacer.finder.immutable.ImmutableFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
import es.bvalero.replacer.finder.util.LinearMatchResult;
import java.util.Set;
import java.util.regex.MatchResult;
import javax.annotation.Resource;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Find person names which are used also as nouns and thus are false positives,
 * e.g. in Spanish `Julio` in `Julio Verne`, as "julio" is also the name of a month
 * to be written in lowercase.
 * <br />
 * It also finds words used commonly in titles, as `Sky` in `Sky News`.
 * Or compound words, as `Los Angeles`.
 */
@Component
class PersonNameFinder implements ImmutableFinder {

    @Resource
    private Set<String> personNames;

    @Override
    public FinderPriority getPriority() {
        // It should be High for number of matches, but it is slow, so it is better to have lower priority.
        return FinderPriority.MEDIUM;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterable<Immutable> find(WikipediaPage page) {
        // The list will keep on growing
        // For the moment the best approach is to iterate the list of words and find them in the text
        return IterableUtils.chainedIterable(
            personNames
                .stream()
                .map(PersonNameLinearFinder::new)
                .map(finder -> finder.find(page))
                .toArray(Iterable[]::new)
        );
    }

    @Override
    public Iterable<MatchResult> findMatchResults(WikipediaPage page) {
        // We are overriding the more general find method
        throw new IllegalCallerException();
    }

    private static class PersonNameLinearFinder implements ImmutableFinder {

        private final String personName;

        PersonNameLinearFinder(String personName) {
            this.personName = personName;
        }

        @Override
        public Iterable<MatchResult> findMatchResults(WikipediaPage page) {
            return LinearMatchFinder.find(page, this::findPersonName);
        }

        @Nullable
        private MatchResult findPersonName(WikipediaPage page, int start) {
            final String text = page.getContent();
            while (start >= 0 && start < text.length()) {
                final int startPersonName = findStartPersonName(text, start);
                if (startPersonName >= 0) {
                    if (FinderUtils.isWordFollowedByUpperCase(startPersonName, personName, text)) {
                        return LinearMatchResult.of(startPersonName, personName);
                    } else {
                        start = startPersonName + personName.length();
                    }
                } else {
                    return null;
                }
            }
            return null;
        }

        private int findStartPersonName(String text, int start) {
            return text.indexOf(personName, start);
        }
    }
}
