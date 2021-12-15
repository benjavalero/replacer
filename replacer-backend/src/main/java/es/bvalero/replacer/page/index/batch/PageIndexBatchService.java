package es.bvalero.replacer.page.index.batch;

import es.bvalero.replacer.common.domain.WikipediaPageId;
import es.bvalero.replacer.page.index.PageIndexResult;
import es.bvalero.replacer.page.index.PageIndexService;
import es.bvalero.replacer.page.index.PageIndexer;
import es.bvalero.replacer.repository.PageModel;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Qualifier("pageBatchIndexService")
@Service
class PageIndexBatchService extends PageIndexService implements PageIndexer {

    @Autowired
    private PageIndexCacheHelper pageIndexCacheHelper;

    @Autowired
    private PageIndexResultBatchSaver pageIndexResultSaver;

    @Override
    protected Optional<PageModel> findByPageId(WikipediaPageId pageId) {
        return pageIndexCacheHelper.findPageById(pageId);
    }

    @Override
    protected void saveResult(PageIndexResult result) {
        pageIndexResultSaver.save(result);
    }

    @Override
    public void finish() {
        pageIndexResultSaver.forceSave();
        pageIndexCacheHelper.resetCache();
    }
}
