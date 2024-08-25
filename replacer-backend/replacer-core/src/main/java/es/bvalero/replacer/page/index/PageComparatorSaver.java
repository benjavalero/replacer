package es.bvalero.replacer.page.index;

import es.bvalero.replacer.page.IndexedPage;
import es.bvalero.replacer.page.save.PageSaveRepository;
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
    void save(IndexedPage indexedPage) {
        addResultToBatch(indexedPage);
        saveBatchResult();
    }

    /* Save in DB the results of page indexing when the result size is large enough */
    void saveBatch(IndexedPage indexedPage) {
        addResultToBatch(indexedPage);
        if (this.batchResult.size() >= this.chunkSize) {
            saveBatchResult();
        }
    }

    private void addResultToBatch(IndexedPage indexedPage) {
        // Add the indexed page to the batch result
        // Just in case we check it the page will actually be saved,
        // not to increase unnecessarily the heap use.
        if (indexedPage.isPageToSave()) {
            this.batchResult.add(indexedPage);
        }
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
