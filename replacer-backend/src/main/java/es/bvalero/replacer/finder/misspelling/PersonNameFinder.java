package es.bvalero.replacer.finder.misspelling;

import es.bvalero.replacer.finder.FinderUtils;
import es.bvalero.replacer.finder.Immutable;
import es.bvalero.replacer.finder.ImmutableFinder;
import es.bvalero.replacer.finder.ImmutableFinderPriority;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * Find person names which are used also as nouns and thus are false positives,
 * e.g. in Spanish `Julio` in `Julio Verne`, as "julio" is also the name of a month
 * to be written in lowercase.
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
    public Iterable<Immutable> find(String text, WikipediaLanguage lang) {
        return personNames.stream().flatMap(name -> findResults(text, name).stream()).collect(Collectors.toList());
    }

    private List<Immutable> findResults(String text, String personName) {
        List<Immutable> results = new ArrayList<>();
        int start = 0;
        while (start >= 0) {
            start = text.indexOf(personName, start);
            if (start >= 0) { // Word found
                if (FinderUtils.isWordFollowedByUppercase(start, personName, text)) {
                    results.add(Immutable.of(start, personName, this));
                }
                start += personName.length();
            }
        }
        return results;
    }
}
