package es.bvalero.replacer.index;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.StandardType;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import org.jmolecules.architecture.hexagonal.PrimaryPort;

@PrimaryPort
public interface PageIndexApi {
    /** Index a page. Replacements and details in database (if any) will be calculated. */
    PageIndexResult indexPage(IndexablePage page);

    default PageIndexResult indexPage(WikipediaPage wikipediaPage) {
        return indexPage(IndexablePage.of(wikipediaPage));
    }

    /* Force saving what is left on the batch (if applicable) */
    default void finish() {
        // Do nothing
    }

    default void indexType(WikipediaLanguage lang, StandardType type) {
        // Do nothing
    }
}
