package es.bvalero.replacer.page;

/** Enumerate to handle the indexation status of an indexable page */
public enum IndexedPageStatus {
    UNDEFINED, // Just retrieved or created
    ADD, // To be added to the repository
    UPDATE, // To be updated in the repository (title or last update)
    MODIFIED, // To be updated in the repository (review timestamp)
    INDEXED, // Already indexed in the repository and no changes needed
}
