package es.bvalero.replacer.finder;

import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import java.util.List;
import java.util.regex.MatchResult;
import org.apache.commons.collections4.IterableUtils;
import org.jetbrains.annotations.TestOnly;

/**
 * Interface to be implemented by any class returning a collection of replacements.
 */
public interface ReplacementFinder {
    Iterable<Replacement> find(String text, WikipediaLanguage lang);

    @TestOnly
    default List<Replacement> findList(String text) {
        return IterableUtils.toList(find(text, WikipediaLanguage.SPANISH));
    }

    @TestOnly
    default List<Replacement> findList(String text, WikipediaLanguage lang) {
        return IterableUtils.toList(find(text, lang));
    }

    default boolean isValidMatch(MatchResult match, String text) {
        return FinderUtils.isWordCompleteInText(match.start(), match.group(), text);
    }
}
