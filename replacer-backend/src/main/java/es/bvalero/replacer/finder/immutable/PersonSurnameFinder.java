package es.bvalero.replacer.finder.immutable;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.MatchResult;
import javax.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * Find person surnames. Also usual nouns preceded by a word starting in uppercase,
 * e.g. in Spanish `RCA Records`, as "records" is also a noun to be written with an accent.
 *
 * The list will keep on growing. For the moment the best approach is to iterate
 * the list of words and find them in the text with `String.indexOf`.
 */
@Component
class PersonSurnameFinder implements ImmutableFinder {

    @Resource
    private Set<String> personSurnames;

    @Override
    public ImmutableFinderPriority getPriority() {
        return ImmutableFinderPriority.HIGH;
    }

    @Override
    public Iterable<Immutable> find(FinderPage page) {
        List<Immutable> result = new ArrayList<>(100);
        for (String personSurname : personSurnames) {
            result.addAll(findResults(page.getContent(), personSurname));
        }
        return result;
    }

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        // We are overriding the more general find method
        throw new IllegalCallerException();
    }

    private List<Immutable> findResults(String text, String surname) {
        List<Immutable> results = new ArrayList<>(100);
        int start = 0;
        while (start >= 0 && start < text.length()) {
            start = text.indexOf(surname, start);
            if (start >= 0) { // Surname found
                if (FinderUtils.isWordPrecededByUppercase(start, surname, text)) {
                    results.add(Immutable.of(start, surname));
                }
                start += surname.length() + 1;
            }
        }
        return results;
    }
}
