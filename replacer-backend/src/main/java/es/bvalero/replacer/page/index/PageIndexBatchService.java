package es.bvalero.replacer.page.index;

import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.common.domain.WikipediaPageId;
import es.bvalero.replacer.repository.PageIndexRepository;
import es.bvalero.replacer.repository.PageModel;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Service
@Qualifier("pageBatchIndexService")
class PageIndexBatchService extends PageIndexAbstractService implements PageIndexService {

    @Qualifier("pageIndexBatchRepository")
    @Autowired
    private PageIndexRepository pageIndexRepository;

    @Autowired
    private PageIndexResultSaver pageIndexResultSaver;

    @Autowired
    private PageIndexValidator pageIndexValidator;

    @Override
    Optional<PageModel> findByPageId(WikipediaPageId pageId) {
        return pageIndexRepository.findPageById(pageId);
    }

    @Override
    void saveResult(PageIndexResult result) {
        pageIndexResultSaver.saveBatch(result);
    }

    @Override
    PageIndexResult indexPage(WikipediaPage page, @Nullable IndexablePage dbPage) {
        // Consider as "not indexed" all indexable pages which are not worth to be re-indexed
        // because they have already been indexed recently in the database
        if (!isPageToBeIndexed(page, dbPage)) {
            return PageIndexResult.builder().status(PageIndexStatus.PAGE_NOT_INDEXED).build();
        }

        return super.indexPage(page, dbPage);
    }

    private boolean isPageToBeIndexed(WikipediaPage page, @Nullable IndexablePage dbPage) {
        // We assume at this point that the page is indexable
        // Check if the page will be re-indexed (by timestamp)
        // Page will also be indexed in case the title is not aligned
        return (
            pageIndexValidator.isIndexableByTimestamp(page, dbPage) ||
            pageIndexValidator.isIndexableByPageTitle(page, dbPage)
        );
    }

    @Override
    public void finish() {
        pageIndexResultSaver.forceSave();
    }
}
