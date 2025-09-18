package es.bvalero.replacer.index;

import es.bvalero.replacer.common.domain.PageKey;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.ReplacementFindApi;
import es.bvalero.replacer.page.IndexedPage;
import es.bvalero.replacer.page.PageSaveRepository;
import java.util.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Qualifier("pageIndexBatchService")
@Service
class PageIndexBatchService extends PageIndexAbstractService implements PageIndexApi {

    // Dependency injection
    private final PageSaveRepository pageSaveRepository;
    private final PageIndexValidator pageIndexValidator;
    private final ReplacementFindApi replacementFindApi;
    private final PageBatchService pageBatchService;

    @Value("${replacer.dump.batch.chunk.size}")
    private int chunkSize;

    // Singleton field to add the items until the batch limit is reached
    private final List<IndexedPage> batchResult = Collections.synchronizedList(new ArrayList<>());

    public PageIndexBatchService(
        PageSaveRepository pageSaveRepository,
        PageIndexValidator pageIndexValidator,
        ReplacementFindApi replacementFindApi,
        PageComparator pageComparator,
        PageBatchService pageBatchService
    ) {
        super(pageSaveRepository, pageIndexValidator, replacementFindApi, pageComparator);
        this.pageSaveRepository = pageSaveRepository;
        this.pageIndexValidator = pageIndexValidator;
        this.replacementFindApi = replacementFindApi;
        this.pageBatchService = pageBatchService;
    }

    @Override
    Optional<IndexedPage> findIndexedPageByKey(PageKey pageKey) {
        return pageBatchService.findByKey(pageKey);
    }

    @Override
    void saveResult(IndexedPage indexedPage) {
        saveBatch(indexedPage);
    }

    /* Save in DB the results of page indexing when the result size is large enough */
    private void saveBatch(IndexedPage indexedPage) {
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

    @Override
    PageIndexResult indexPage(IndexablePage indexablePage, @Nullable IndexedPage dbPage) {
        // Consider as "not indexed" all indexable pages which are not worth to be re-indexed
        // because they have already been indexed recently in the database
        if (!isPageToBeIndexed(indexablePage, dbPage)) {
            return PageIndexResult.ofNotIndexed();
        }

        return super.indexPage(indexablePage, dbPage);
    }

    private boolean isPageToBeIndexed(IndexablePage page, @Nullable IndexedPage dbPage) {
        // We assume at this point that the page is indexable by itself
        return pageIndexValidator.isIndexableByTimestamp(page, dbPage);
    }

    SortedSet<Replacement> findReplacements(IndexablePage indexablePage) {
        return replacementFindApi.findReplacementsWithoutSuggestions(indexablePage.toFinderPage());
    }

    @Override
    public void finish() {
        // Force saving what is left on the batch
        saveBatchResult();
    }
}
