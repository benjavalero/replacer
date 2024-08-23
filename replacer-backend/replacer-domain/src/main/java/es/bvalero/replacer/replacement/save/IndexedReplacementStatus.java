package es.bvalero.replacer.replacement.save;

/** Enumerate to handle the indexation status of an indexable replacement */
public enum IndexedReplacementStatus {
    UNDEFINED, // Just retrieved or created
    ADD, // To be added to the repository
    UPDATE, // To be updated in the repository (start or context)
    REMOVE, // To be removed from the repository
    INDEXED, // Already indexed in the repository and no changes needed
}
