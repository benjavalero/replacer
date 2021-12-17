package es.bvalero.replacer.page.index;

import es.bvalero.replacer.repository.PageRepository;
import es.bvalero.replacer.repository.ReplacementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Save in database the results of page indexing.
 * When batch-saving, the results will be saved if the result size is large enough.
 */
@Component
class PageIndexResultSaver {

    @Autowired
    PageRepository pageRepository;

    @Autowired
    ReplacementRepository replacementRepository;

    @Value("${replacer.dump.batch.chunk.size}")
    private int chunkSize;

    // Singleton field to add the items until the batch limit is reached
    private final PageIndexResult batchResult = PageIndexResult.ofEmpty();

    /* Save in DB the result of a page indexing no matter the size of the result */
    void save(PageIndexResult pageIndexResult) {
        this.addResultToBatch(pageIndexResult);
        this.saveBatchResult();
    }

    /* Save in DB the results of page indexing when the result size is large enough */
    void saveBatch(PageIndexResult pageIndexResult) {
        this.addResultToBatch(pageIndexResult);
        if (this.getBatchSize() >= chunkSize) {
            this.saveBatchResult();
        }
    }

    private void addResultToBatch(PageIndexResult pageIndexResult) {
        // Add all the items to the batch result
        this.batchResult.add(pageIndexResult);
    }

    private int getBatchSize() {
        return this.batchResult.size();
    }

    private void saveBatchResult() {
        // Pages must be added before adding the related replacements
        // Pages are removed along with their replacements
        // We assume the replacements removed correspond to not removed pages
        pageRepository.addPages(IndexablePageMapper.toModel(batchResult.getAddPages()));
        pageRepository.updatePages(IndexablePageMapper.toModel(batchResult.getUpdatePages()));
        replacementRepository.addReplacements(IndexableReplacementMapper.toModel(batchResult.getAddReplacements()));
        replacementRepository.updateReplacements(
            IndexableReplacementMapper.toModel(batchResult.getUpdateReplacements())
        );
        replacementRepository.removeReplacements(
            IndexableReplacementMapper.toModel(batchResult.getRemoveReplacements())
        );

        this.clearBatchResult();
    }

    private void clearBatchResult() {
        this.batchResult.clear();
    }

    /* Force saving what is left on the batch */
    void forceSave() {
        this.saveBatchResult();
    }
}
