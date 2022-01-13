package es.bvalero.replacer.finder.immutable.finders;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.immutable.Immutable;
import es.bvalero.replacer.finder.immutable.ImmutableFinder;
import es.bvalero.replacer.finder.immutable.ImmutableFinderPriority;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
import es.bvalero.replacer.finder.util.LinearMatchResult;
import java.util.ArrayList;
import java.util.List;
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
 *
 * It also finds words used commonly in titles, as `Sky` in `Sky News`.
 * Or compound words, as `Los Angeles`.
 */
@Component
class PersonNameFinder implements ImmutableFinder {

    @Resource
    private Set<String> personNames;

    @Override
    public ImmutableFinderPriority getPriority() {
        // It should be High for number of matches but it is slow so it is better to have lower priority
        return ImmutableFinderPriority.MEDIUM;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterable<Immutable> find(FinderPage page) {
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
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        // We are overriding the more general find method
        throw new IllegalCallerException();
    }

    private static class PersonNameLinearFinder implements ImmutableFinder {

        private final String personName;

        PersonNameLinearFinder(String personName) {
            this.personName = personName;
        }

        @Override
        public Iterable<MatchResult> findMatchResults(FinderPage page) {
            return LinearMatchFinder.find(page, this::findResult);
        }

        @Nullable
        private MatchResult findResult(FinderPage page, int start) {
            final List<MatchResult> matches = new ArrayList<>();
            while (start >= 0 && start < page.getContent().length() && matches.isEmpty()) {
                start = findPersonName(page, start, personName, matches);
            }
            return matches.isEmpty() ? null : matches.get(0);
        }

        private int findPersonName(FinderPage page, int start, String personName, List<MatchResult> matches) {
            final String text = page.getContent();
            final int personNameStart = text.indexOf(personName, start);
            if (personNameStart >= 0) {
                if (FinderUtils.isWordFollowedByUppercase(personNameStart, personName, text)) {
                    matches.add(LinearMatchResult.of(personNameStart, personName));
                }
                return personNameStart + personName.length();
            } else {
                return -1;
            }
        }
    }
}
