package es.bvalero.replacer.page.index;

import es.bvalero.replacer.repository.PageModel;
import es.bvalero.replacer.repository.PageRepository;
import es.bvalero.replacer.repository.ReplacementModel;
import es.bvalero.replacer.repository.ReplacementRepository;
import java.util.*;
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
    private final List<PageIndexResult> batchResult = Collections.synchronizedList(new ArrayList<>());

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
        // We use a good-old "for" loop in order to avoid a ConcurrentModificationException
        int size = 0;
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < this.batchResult.size(); i++) {
            size += this.batchResult.get(i).size();
        }
        return size;
    }

    private void saveBatchResult() {
        // We use a good-old "for" loop in order to avoid a ConcurrentModificationException
        // See: https://medium.com/xiumeteo-labs/stream-and-concurrentmodificationexception-2d14ed8ff4b2
        // Also use sets just in case to prevent duplicated insertions
        final Set<PageModel> addPages = new HashSet<>();
        final Set<PageModel> updatePages = new HashSet<>();
        final Set<ReplacementModel> addReplacements = new HashSet<>();
        final Set<ReplacementModel> updateReplacements = new HashSet<>();
        final Set<ReplacementModel> removeReplacements = new HashSet<>();

        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < this.batchResult.size(); i++) {
            PageIndexResult result = this.batchResult.get(i);
            addPages.addAll(IndexablePageMapper.toModel(result.getAddPages()));
            updatePages.addAll(IndexablePageMapper.toModel(result.getUpdatePages()));
            addReplacements.addAll(IndexableReplacementMapper.toModel(result.getAddReplacements()));
            updateReplacements.addAll(IndexableReplacementMapper.toModel(result.getUpdateReplacements()));
            removeReplacements.addAll(IndexableReplacementMapper.toModel(result.getRemoveReplacements()));
        }

        // Pages must be added before adding the related replacements
        // Pages are removed along with their replacements
        // We assume the replacements removed correspond to not removed pages
        pageRepository.addPages(addPages);
        pageRepository.updatePages(updatePages);
        replacementRepository.addReplacements(addReplacements);
        replacementRepository.updateReplacements(updateReplacements);
        replacementRepository.removeReplacements(removeReplacements);

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
