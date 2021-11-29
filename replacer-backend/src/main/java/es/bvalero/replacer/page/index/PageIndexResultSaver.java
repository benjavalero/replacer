package es.bvalero.replacer.page.index;

import es.bvalero.replacer.page.repository.PageRepository;
import es.bvalero.replacer.page.repository.ReplacementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Save in database the results of page indexing.
 * When batch-saving, the results will be saved if the result size is large enough.
 */
@Component
public class PageIndexResultSaver {

    @Autowired
    PageRepository pageRepository;

    @Autowired
    ReplacementRepository replacementRepository;

    // Singleton field to add the items until the batch limit is reached
    private final PageIndexResult batchResult = PageIndexResult.ofEmpty();

    /* Save in DB the result of a page indexing no matter the size of the result */
    protected void save(PageIndexResult pageIndexResult) {
        this.addResultToBatch(pageIndexResult);
        this.saveBatchResult();
    }

    protected void addResultToBatch(PageIndexResult pageIndexResult) {
        // Add all the items to the batch result
        this.batchResult.add(pageIndexResult);
    }

    protected int getBatchSize() {
        return this.batchResult.size();
    }

    protected void saveBatchResult() {
        pageRepository.insertPages(IndexablePageMapper.toModel(batchResult.getCreatePages()));
        pageRepository.updatePageTitles(IndexablePageMapper.toModel(batchResult.getUpdatePages()));
        pageRepository.deletePages(IndexablePageMapper.toModel(batchResult.getDeletePages()));
        replacementRepository.insertReplacements(
            IndexableReplacementMapper.toModel(batchResult.getCreateReplacements())
        );
        replacementRepository.updateReplacements(
            IndexableReplacementMapper.toModel(batchResult.getUpdateReplacements())
        );
        replacementRepository.deleteReplacements(
            IndexableReplacementMapper.toModel(batchResult.getDeleteReplacements())
        );

        this.clearBatchResult();
    }

    private void clearBatchResult() {
        this.batchResult.clear();
    }
}
