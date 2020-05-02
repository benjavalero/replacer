package es.bvalero.replacer.finder;

import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.commons.collections4.IterableUtils;
import org.jetbrains.annotations.TestOnly;

/**
 * Interface to be implemented by any class returning a collection of replacements.
 */
public interface ReplacementFinder {
    Iterable<Replacement> find(String text, WikipediaLanguage lang);

    default Stream<Replacement> findStream(String text, WikipediaLanguage lang) {
        return StreamSupport.stream(find(text, lang).spliterator(), false);
    }

    @TestOnly
    default List<Replacement> findList(String text) {
        return IterableUtils.toList(find(text, WikipediaLanguage.ALL));
    }

    default List<Replacement> findList(String text, WikipediaLanguage lang) {
        return IterableUtils.toList(find(text, lang));
    }

    default boolean isValidMatch(MatchResult match, String text) {
        return FinderUtils.isWordCompleteInText(match.start(), match.group(), text);
    }
}
