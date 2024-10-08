package es.bvalero.replacer.page.index;

import es.bvalero.replacer.page.IndexedPage;
import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.page.save.PageSaveRepository;
import java.util.Optional;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Service
public class PageIndexBatchService extends PageIndexAbstractService {

    // Dependency injection
    private final PageIndexValidator pageIndexValidator;
    private final PageBatchService pageBatchService;
    private final PageComparatorSaver pageComparatorSaver;

    public PageIndexBatchService(
        PageSaveRepository pageSaveRepository,
        PageIndexValidator pageIndexValidator,
        ReplacementFindService replacementFindService,
        PageComparator pageComparator,
        PageBatchService pageBatchService,
        PageComparatorSaver pageComparatorSaver
    ) {
        super(pageSaveRepository, pageIndexValidator, replacementFindService, pageComparator);
        this.pageIndexValidator = pageIndexValidator;
        this.pageBatchService = pageBatchService;
        this.pageComparatorSaver = pageComparatorSaver;
    }

    @Override
    Optional<IndexedPage> findIndexedPageByKey(PageKey pageKey) {
        return pageBatchService.findByKey(pageKey);
    }

    @Override
    void saveResult(IndexedPage indexedPage) {
        pageComparatorSaver.saveBatch(indexedPage);
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

    /* Force saving what is left on the batch (if applicable) */
    public void finish() {
        pageComparatorSaver.forceSave();
    }
}
