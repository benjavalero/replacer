package es.bvalero.replacer.page.index;

import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.page.IndexedPage;
import es.bvalero.replacer.page.PageRepository;
import es.bvalero.replacer.page.count.PageCountRepository;
import es.bvalero.replacer.replacement.IndexedReplacement;
import es.bvalero.replacer.replacement.ReplacementSaveRepository;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Save in database the results of page indexing.
 * When batch-saving, the results will be saved if the result size is large enough.
 */
@Component
class PageComparatorSaver {

    // Dependency injection
    private final PageRepository pageRepository;
    private final PageCountRepository pageCountRepository;
    private final ReplacementSaveRepository replacementSaveRepository;

    @Value("${replacer.dump.batch.chunk.size}")
    private int chunkSize;

    // Singleton field to add the items until the batch limit is reached
    private final List<PageComparatorResult> batchResult = Collections.synchronizedList(new ArrayList<>());

    PageComparatorSaver(
        PageRepository pageRepository,
        PageCountRepository pageCountRepository,
        ReplacementSaveRepository replacementSaveRepository
    ) {
        this.pageRepository = pageRepository;
        this.pageCountRepository = pageCountRepository;
        this.replacementSaveRepository = replacementSaveRepository;
    }

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
        final Set<IndexedPage> pagesToCreate = new HashSet<>();
        final Set<IndexedPage> pagesToUpdate = new HashSet<>();
        final Set<IndexedReplacement> replacementsToCreate = new HashSet<>();
        final Set<IndexedReplacement> replacementsToUpdate = new HashSet<>();
        final Set<IndexedReplacement> replacementsToDelete = new HashSet<>();

        synchronized (this.batchResult) {
            for (PageComparatorResult result : this.batchResult) {
                pagesToCreate.addAll(result.getPagesToCreate());
                pagesToUpdate.addAll(result.getPagesToUpdate());
                replacementsToCreate.addAll(result.getReplacementsToCreate());
                replacementsToUpdate.addAll(result.getReplacementsToUpdate());
                replacementsToDelete.addAll(result.getReplacementsToDelete());

                // Update page count cache
                result
                    .getReplacementTypesToCreate()
                    .forEach(rt -> pageCountRepository.incrementPageCountByType(result.getLang(), (StandardType) rt));
                result
                    .getReplacementTypesToDelete()
                    .forEach(rt -> pageCountRepository.decrementPageCountByType(result.getLang(), (StandardType) rt));
            }

            this.batchResult.clear();
        }

        // Pages must be added before adding the related replacements
        // We assume the replacements removed correspond to not removed pages
        pageRepository.add(pagesToCreate);
        pageRepository.update(pagesToUpdate);
        replacementSaveRepository.add(replacementsToCreate);
        replacementSaveRepository.update(replacementsToUpdate);
        replacementSaveRepository.remove(replacementsToDelete);
    }

    /* Force saving what is left on the batch */
    void forceSave() {
        saveBatchResult();
    }
}
