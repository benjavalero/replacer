package es.bvalero.replacer.finder;

import es.bvalero.replacer.page.IndexablePage;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import java.util.List;
import java.util.regex.MatchResult;
import org.apache.commons.collections4.IterableUtils;
import org.jetbrains.annotations.TestOnly;

/**
 * Interface to be implemented by any class returning a collection of replacements.
 */
public interface ReplacementFinder {
    Iterable<Replacement> find(IndexablePage page);

    default List<Replacement> findList(IndexablePage page) {
        return IterableUtils.toList(find(page));
    }

    @TestOnly
    default List<Replacement> findList(String text) {
        WikipediaPage page = WikipediaPage.builder().content(text).lang(WikipediaLanguage.getDefault()).build();
        return IterableUtils.toList(find(page));
    }

    @TestOnly
    default List<Replacement> findList(String text, WikipediaLanguage lang) {
        WikipediaPage page = WikipediaPage.builder().content(text).lang(lang).build();
        return IterableUtils.toList(find(page));
    }

    default boolean isValidMatch(MatchResult match, IndexablePage page) {
        return FinderUtils.isWordCompleteInText(match.start(), match.group(), page.getContent());
    }
}
