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
 * Find person names which are used also as nouns and thus are false positives,
 * e.g. in Spanish `Julio` in `Julio Verne`, as "julio" is also the name of a month
 * to be written in lowercase.
 *
 * It also finds words used commonly in titles, as `Sky` in `Sky News`.
 * Or compound words, as `Los Angeles`.
 *
 * The list will keep on growing. For the moment the best approach is to iterate
 * the list of words and find them in the text with `String.indexOf`.
 */
@Component
public class PersonNameFinder implements ImmutableFinder {

    @Resource
    private Set<String> personNames;

    @Override
    public ImmutableFinderPriority getPriority() {
        return ImmutableFinderPriority.LOW;
    }

    @Override
    public Iterable<Immutable> find(IndexablePage page) {
        List<Immutable> result = new ArrayList<>(100);
        for (String personName : personNames) {
            result.addAll(findResults(page.getContent(), personName));
        }
        return result;
    }

    private List<Immutable> findResults(String text, String personName) {
        List<Immutable> results = new ArrayList<>(100);
        int start = 0;
        while (start >= 0 && start < text.length()) {
            start = text.indexOf(personName, start);
            if (start >= 0) { // Person name found
                if (FinderUtils.isWordFollowedByUppercase(start, personName, text)) {
                    results.add(Immutable.of(start, personName, this));
                }
                start += personName.length() + 1;
            }
        }
        return results;
    }
}
