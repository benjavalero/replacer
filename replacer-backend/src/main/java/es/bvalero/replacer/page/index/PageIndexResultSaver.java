package es.bvalero.replacer.page.index;

import es.bvalero.replacer.page.repository.IndexablePageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PageIndexResultSaver {

    @Autowired
    IndexablePageRepository indexablePageRepository;

    @Value("${replacer.dump.batch.chunk.size}")
    private int chunkSize;

    // Singleton field to add the items until the batch limit is reached
    private final PageIndexResult batchResult = PageIndexResult.ofEmpty();

    /* Save in DB the result of a page indexing no matter the size of the batch */
    public void save(PageIndexResult pageIndexResult) {
        this.addResultToBatch(pageIndexResult);
        this.saveBatchResult();
    }

    /* Save in DB the results of page indexing when the batch is large enough */
    public void saveBatch(PageIndexResult pageIndexResult) {
        this.addResultToBatch(pageIndexResult);
        if (this.batchResult.size() >= chunkSize) {
            this.saveBatchResult();
        }
    }

    /* Force saving what is left on the batch */
    public void forceSave() {
        this.saveBatchResult();
    }

    private void addResultToBatch(PageIndexResult pageIndexResult) {
        // Add all the items to the batch result
        this.batchResult.add(pageIndexResult);
    }

    private void saveBatchResult() {
        indexablePageRepository.insertPages(batchResult.getCreatePages());
        indexablePageRepository.updatePageTitles(batchResult.getUpdatePages());
        indexablePageRepository.insertReplacements(batchResult.getCreateReplacements());
        indexablePageRepository.updateReplacements(batchResult.getUpdateReplacements());
        indexablePageRepository.deleteReplacements(batchResult.getDeleteReplacements());

        this.clearBatchResult();
    }

    private void clearBatchResult() {
        this.batchResult.clear();
    }
}
