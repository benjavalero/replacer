package es.bvalero.replacer.page.index.batch;

import es.bvalero.replacer.page.index.PageIndexResult;
import es.bvalero.replacer.page.index.PageIndexResultSaver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Save in database the results of page indexing.
 * When batch-saving, the results will be saved if the result size is large enough.
 */
@Component
class PageIndexResultBatchSaver extends PageIndexResultSaver {

    @Value("${replacer.dump.batch.chunk.size}")
    private int chunkSize;

    /* Save in DB the results of page indexing when the result size is large enough */
    @Override
    protected void save(PageIndexResult pageIndexResult) {
        this.addResultToBatch(pageIndexResult);
        if (this.getBatchSize() >= chunkSize) {
            this.saveBatchResult();
        }
    }

    /* Force saving what is left on the batch */
    void forceSave() {
        this.saveBatchResult();
    }
}
