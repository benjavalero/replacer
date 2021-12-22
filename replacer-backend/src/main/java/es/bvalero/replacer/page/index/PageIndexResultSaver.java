package es.bvalero.replacer.page.index;

import es.bvalero.replacer.repository.PageRepository;
import es.bvalero.replacer.repository.ReplacementRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
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
        return this.batchResult.stream().mapToInt(PageIndexResult::size).sum();
    }

    private void saveBatchResult() {
        // Pages must be added before adding the related replacements
        // Pages are removed along with their replacements
        // We assume the replacements removed correspond to not removed pages
        pageRepository.addPages(
            IndexablePageMapper.toModel(
                batchResult.stream().flatMap(r -> r.getAddPages().stream()).collect(Collectors.toUnmodifiableSet())
            )
        );
        pageRepository.updatePages(
            IndexablePageMapper.toModel(
                batchResult.stream().flatMap(r -> r.getUpdatePages().stream()).collect(Collectors.toUnmodifiableSet())
            )
        );
        replacementRepository.addReplacements(
            IndexableReplacementMapper.toModel(
                batchResult
                    .stream()
                    .flatMap(r -> r.getAddReplacements().stream())
                    .collect(Collectors.toUnmodifiableSet())
            )
        );
        replacementRepository.updateReplacements(
            IndexableReplacementMapper.toModel(
                batchResult
                    .stream()
                    .flatMap(r -> r.getUpdateReplacements().stream())
                    .collect(Collectors.toUnmodifiableSet())
            )
        );
        replacementRepository.removeReplacements(
            IndexableReplacementMapper.toModel(
                batchResult
                    .stream()
                    .flatMap(r -> r.getRemoveReplacements().stream())
                    .collect(Collectors.toUnmodifiableSet())
            )
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
