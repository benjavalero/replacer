package es.bvalero.replacer.index;

public interface PageIndexService {
    /** Index a page. Replacements and details in database (if any) will be calculated. */
    PageIndexResult indexPage(IndexablePage page);

    /* Force saving what is left on the batch (if applicable) */
    void finish();
}
