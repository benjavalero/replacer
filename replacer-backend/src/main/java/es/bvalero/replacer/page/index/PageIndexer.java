package es.bvalero.replacer.page.index;

import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.common.domain.WikipediaPageId;

/** Index a page: calculate its replacements and update the related replacements in DB accordingly */
public interface PageIndexer {
    /** Index a page. Replacements and details in database (if any) will be calculated. */
    PageIndexResult indexPageReplacements(WikipediaPage page);

    /** Index a page which should not be in database because it has been deleted or is not indexable anymore */
    void indexObsoletePage(WikipediaPageId pageId);

    /* Force saving what is left on the batch (if applicable) */
    void forceSave();
}
