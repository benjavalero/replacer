package es.bvalero.replacer.dump;

enum DumpPageIndexResult {
    PAGE_NOT_INDEXABLE,
    PAGE_NOT_INDEXED, // Indexable but not indexed, e.g. by date.
    PAGE_INDEXED,
}
