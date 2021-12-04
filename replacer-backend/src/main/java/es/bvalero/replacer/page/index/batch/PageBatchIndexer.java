package es.bvalero.replacer.page.index.batch;

import es.bvalero.replacer.common.domain.WikipediaPageId;
import es.bvalero.replacer.page.index.PageIndexResult;
import es.bvalero.replacer.page.index.PageIndexer;
import es.bvalero.replacer.repository.PageModel;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class PageBatchIndexer extends PageIndexer {

    @Autowired
    @Qualifier("pageCacheRepository")
    private PageCacheRepository pageCacheRepository;

    @Autowired
    private PageIndexResultBatchSaver pageIndexResultSaver;

    @Override
    protected Optional<PageModel> findByPageId(WikipediaPageId pageId) {
        return pageCacheRepository.findByPageId(pageId);
    }

    @Override
    protected void saveResult(PageIndexResult result) {
        pageIndexResultSaver.save(result);
    }

    /* Force saving what is left on the batch (if applicable) */
    public void forceSave() {
        pageIndexResultSaver.forceSave();
        pageCacheRepository.resetCache();
    }
}
