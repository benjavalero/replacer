package es.bvalero.replacer.page.index;

import es.bvalero.replacer.page.IndexedPage;
import es.bvalero.replacer.page.PageBatchService;
import es.bvalero.replacer.page.PageKey;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Service
public class PageIndexBatchService extends PageIndexAbstractService {

    @Autowired
    private PageBatchService pageBatchService;

    @Autowired
    private PageComparatorSaver pageComparatorSaver;

    @Autowired
    private PageIndexValidator pageIndexValidator;

    @Override
    Optional<IndexedPage> findIndexedPageByKey(PageKey pageKey) {
        return pageBatchService.findPageByKey(pageKey);
    }

    @Override
    void saveResult(PageComparatorResult result) {
        pageComparatorSaver.saveBatch(result);
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
