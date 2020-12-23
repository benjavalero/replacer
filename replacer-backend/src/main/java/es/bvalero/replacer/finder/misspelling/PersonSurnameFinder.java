package es.bvalero.replacer.finder.misspelling;

import es.bvalero.replacer.finder.FinderUtils;
import es.bvalero.replacer.finder.Immutable;
import es.bvalero.replacer.finder.ImmutableFinder;
import es.bvalero.replacer.finder.ImmutableFinderPriority;
import es.bvalero.replacer.page.IndexablePage;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
public class PersonSurnameFinder implements ImmutableFinder {

    @Resource
    private Set<String> personSurnames;

    @Override
    public ImmutableFinderPriority getPriority() {
        return ImmutableFinderPriority.LOW;
    }

    @Override
    public Iterable<Immutable> find(IndexablePage page) {
        List<Immutable> result = new ArrayList<>(100);
        for (String personSurname : personSurnames) {
            result.addAll(findResults(page.getContent(), personSurname));
        }
        return result;
    }

    private List<Immutable> findResults(String text, String surname) {
        List<Immutable> results = new ArrayList<>(100);
        int start = 0;
        while (start >= 0 && start < text.length()) {
            start = text.indexOf(surname, start);
            if (start >= 0) { // Surname found
                if (FinderUtils.isWordPrecededByUppercase(start, surname, text)) {
                    results.add(Immutable.of(start, surname, this));
                }
                start += surname.length() + 1;
            }
        }
        return results;
    }
}
