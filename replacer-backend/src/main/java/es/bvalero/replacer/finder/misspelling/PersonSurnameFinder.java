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
 * Find person surnames. Also usual nouns preceded by a word starting in uppercase,
 * e.g. `RCA Records`
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
    public Iterable<Immutable> find(String text, WikipediaLanguage lang) {
        return personSurnames
            .stream()
            .flatMap(surname -> findResults(text, surname).stream())
            .collect(Collectors.toList());
    }

    private List<Immutable> findResults(String text, String surname) {
        List<Immutable> results = new ArrayList<>();
        int start = 0;
        while (start >= 0) {
            start = text.indexOf(surname, start);
            if (start >= 0) { // Word found
                if (FinderUtils.isWordPrecededByUppercase(start, surname, text)) {
                    results.add(Immutable.of(start, surname, this));
                }
                start += surname.length();
            }
        }
        return results;
    }
}
