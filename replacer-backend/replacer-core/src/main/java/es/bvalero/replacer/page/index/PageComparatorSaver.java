package es.bvalero.replacer.page.index;

import es.bvalero.replacer.page.IndexedPage;
import es.bvalero.replacer.page.PageService;
import es.bvalero.replacer.replacement.IndexedReplacement;
import es.bvalero.replacer.replacement.ReplacementService;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Save in database the results of page indexing.
 * When batch-saving, the results will be saved if the result size is large enough.
 */
@Component
class PageComparatorSaver {

    @Autowired
    private PageService pageService;

    @Autowired
    private ReplacementService replacementService;

    @Value("${replacer.dump.batch.chunk.size}")
    private int chunkSize;

    // Singleton field to add the items until the batch limit is reached
    private final List<PageComparatorResult> batchResult = Collections.synchronizedList(new ArrayList<>());

    /* Save in DB the result of a page indexing no matter the size of the result */
    void save(PageComparatorResult pageComparatorResult) {
        addResultToBatch(pageComparatorResult);
        saveBatchResult();
    }

    /* Save in DB the results of page indexing when the result size is large enough */
    void saveBatch(PageComparatorResult pageComparatorResult) {
        addResultToBatch(pageComparatorResult);
        if (getBatchSize() >= this.chunkSize) {
            saveBatchResult();
        }
    }

    private void addResultToBatch(PageComparatorResult pageComparatorResult) {
        // Add all the items to the batch result
        // Just in case we check it is not empty,
        // not to increase unnecessarily the heap use.
        if (!pageComparatorResult.isEmpty()) {
            this.batchResult.add(pageComparatorResult);
        }
    }

    private int getBatchSize() {
        synchronized (this.batchResult) {
            return this.batchResult.stream().mapToInt(PageComparatorResult::size).sum();
        }
    }

    private void saveBatchResult() {
        // We use a thread-safe loop in order to avoid a ConcurrentModificationException
        // https://www.baeldung.com/java-synchronized-collections
        // Also use sets just in case to prevent duplicated insertions
        final Set<IndexedPage> addPages = new HashSet<>();
        final Set<IndexedPage> updatePages = new HashSet<>();
        final Set<IndexedReplacement> addReplacements = new HashSet<>();
        final Set<IndexedReplacement> updateReplacements = new HashSet<>();
        final Set<IndexedReplacement> removeReplacements = new HashSet<>();

        synchronized (this.batchResult) {
            for (PageComparatorResult result : this.batchResult) {
                addPages.addAll(result.getAddPages());
                updatePages.addAll(result.getUpdatePages());
                addReplacements.addAll(result.getAddReplacements());
                updateReplacements.addAll(result.getUpdateReplacements());
                removeReplacements.addAll(result.getRemoveReplacements());
            }

            this.batchResult.clear();
        }

        // Pages must be added before adding the related replacements
        // Pages are removed along with their replacements
        // We assume the replacements removed correspond to not removed pages
        pageService.addPages(addPages);
        pageService.updatePages(updatePages);
        replacementService.addReplacements(addReplacements);
        replacementService.updateReplacements(updateReplacements);
        replacementService.removeReplacements(removeReplacements);
    }

    /* Force saving what is left on the batch */
    void forceSave() {
        saveBatchResult();
    }
}
