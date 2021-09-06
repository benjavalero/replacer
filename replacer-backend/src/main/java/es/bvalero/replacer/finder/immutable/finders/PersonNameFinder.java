package es.bvalero.replacer.finder.immutable.finders;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.immutable.Immutable;
import es.bvalero.replacer.finder.immutable.ImmutableFinder;
import es.bvalero.replacer.finder.immutable.ImmutableFinderPriority;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.MatchResult;
import javax.annotation.Resource;
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
        return ImmutableFinderPriority.HIGH;
    }

    @Override
    public Iterable<Immutable> find(FinderPage page) {
        List<Immutable> result = new ArrayList<>();
        // The list will keep on growing
        // For the moment the best approach is to iterate the list of words and find them in the text.
        for (String personName : personNames) {
            result.addAll(findResults(page.getContent(), personName));
        }
        return result;
    }

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        // We are overriding the more general find method
        throw new IllegalCallerException();
    }

    private List<Immutable> findResults(String text, String personName) {
        List<Immutable> results = new ArrayList<>();
        int start = 0;
        while (start >= 0 && start < text.length()) {
            start = text.indexOf(personName, start);
            if (start >= 0) { // Person name found
                if (FinderUtils.isWordFollowedByUppercase(start, personName, text)) {
                    results.add(Immutable.of(start, personName));
                }
                start += personName.length() + 1;
            }
        }
        return results;
    }
}
