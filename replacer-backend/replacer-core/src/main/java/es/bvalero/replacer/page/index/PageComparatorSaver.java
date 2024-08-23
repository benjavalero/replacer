package es.bvalero.replacer.page.index;

import es.bvalero.replacer.page.IndexedPage;
import es.bvalero.replacer.page.save.IndexedPageStatus;
import es.bvalero.replacer.page.save.PageSaveRepository;
import es.bvalero.replacer.replacement.save.IndexedReplacementStatus;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Save in database the results of page indexing.
 * When batch-saving, the results will be saved if the result size is large enough.
 */
@Component
class PageComparatorSaver {

    // Dependency injection
    private final PageSaveRepository pageSaveRepository;

    @Value("${replacer.dump.batch.chunk.size}")
    private int chunkSize;

    // Singleton field to add the items until the batch limit is reached
    private final List<IndexedPage> batchResult = Collections.synchronizedList(new ArrayList<>());

    PageComparatorSaver(PageSaveRepository pageSaveRepository) {
        this.pageSaveRepository = pageSaveRepository;
    }

    /* Save in DB the result of a page indexing no matter the size of the result */
    void save(PageComparatorResult pageComparatorResult) {
        addResultToBatch(pageComparatorResult);
        saveBatchResult();
    }

    /* Save in DB the results of page indexing when the result size is large enough */
    void saveBatch(PageComparatorResult pageComparatorResult) {
        addResultToBatch(pageComparatorResult);
        if (this.batchResult.size() >= this.chunkSize) {
            saveBatchResult();
        }
    }

    private void addResultToBatch(PageComparatorResult pageComparatorResult) {
        // Add the indexed page to the batch result
        // Just in case we check it the page will actually be saved,
        // not to increase unnecessarily the heap use.
        IndexedPage pageToSave = pageComparatorResult.getPageToSave();
        if (isPageToSave(pageToSave)) {
            this.batchResult.add(pageToSave);
        }
    }

    private boolean isPageToSave(IndexedPage page) {
        // TODO Not Null
        return (
            (page != null && page.getStatus() != IndexedPageStatus.UNDEFINED) ||
            page.getReplacements().stream().anyMatch(r -> r.getStatus() != IndexedReplacementStatus.UNDEFINED)
        );
    }

    private void saveBatchResult() {
        synchronized (this.batchResult) {
            pageSaveRepository.save(this.batchResult);
            this.batchResult.clear();
        }
    }

    /* Force saving what is left on the batch */
    void forceSave() {
        saveBatchResult();
    }
}
