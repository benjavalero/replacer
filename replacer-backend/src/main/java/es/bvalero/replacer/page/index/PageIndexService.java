package es.bvalero.replacer.page.index;

import es.bvalero.replacer.common.domain.WikipediaPage;

public interface PageIndexService {
    /** Index a page. Replacements and details in database (if any) will be calculated. */
    PageIndexResult indexPage(WikipediaPage page);

    /* Force saving what is left on the batch (if applicable) */
    void finish();
}
