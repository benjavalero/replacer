package es.bvalero.replacer.dump;

enum DumpPageProcessorResult {
    PAGE_NOT_PROCESSABLE,
    PAGE_NOT_PROCESSED, // Processable but not processed, e.g. by date.
    PAGE_PROCESSED,
}
