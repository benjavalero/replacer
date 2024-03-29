package es.bvalero.replacer.page.index;

/** Status of a page after trying to index it */
public enum PageIndexStatus {
    PAGE_NOT_INDEXABLE,
    PAGE_NOT_INDEXED, // Indexable but not indexed, e.g. by date or because there are no changes to update.
    PAGE_INDEXED,
}
