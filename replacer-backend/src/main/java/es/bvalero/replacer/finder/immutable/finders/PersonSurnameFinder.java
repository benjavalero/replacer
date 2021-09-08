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
 * Find person surnames. Also usual nouns preceded by a word starting in uppercase,
 * e.g. in Spanish `RCA Records`, as "records" is also a noun to be written with an accent.
 */
@Component
class PersonSurnameFinder implements ImmutableFinder {

    @Resource
    private Set<String> personSurnames;

    @Override
    public ImmutableFinderPriority getPriority() {
        return ImmutableFinderPriority.HIGH;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterable<Immutable> find(FinderPage page) {
        // The list will keep on growing
        // For the moment the best approach is to iterate the list of words and find them in the text
        return IterableUtils.chainedIterable(
            personSurnames
                .stream()
                .map(PersonSurnameLinearFinder::new)
                .map(finder -> finder.find(page))
                .toArray(Iterable[]::new)
        );
    }

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        // We are overriding the more general find method
        throw new IllegalCallerException();
    }

    private static class PersonSurnameLinearFinder implements ImmutableFinder {

        private final String personSurname;

        PersonSurnameLinearFinder(String personSurname) {
            this.personSurname = personSurname;
        }

        @Override
        public Iterable<MatchResult> findMatchResults(FinderPage page) {
            return LinearMatchFinder.find(page, this::findResult);
        }

        @Nullable
        private MatchResult findResult(FinderPage page, int start) {
            List<MatchResult> matches = new ArrayList<>();
            while (start >= 0 && start < page.getContent().length() && matches.isEmpty()) {
                start = findPersonSurname(page, start, personSurname, matches);
            }
            return matches.isEmpty() ? null : matches.get(0);
        }

        private int findPersonSurname(FinderPage page, int start, String personSurname, List<MatchResult> matches) {
            String text = page.getContent();
            int personSurnameStart = text.indexOf(personSurname, start);
            if (personSurnameStart >= 0) {
                if (FinderUtils.isWordPrecededByUppercase(personSurnameStart, personSurname, text)) {
                    matches.add(LinearMatchResult.of(personSurnameStart, personSurname));
                }
                return personSurnameStart + personSurname.length();
            } else {
                return -1;
            }
        }
    }
}
