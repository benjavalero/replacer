package es.bvalero.replacer.page.index;

import es.bvalero.replacer.common.domain.WikipediaPageId;
import es.bvalero.replacer.repository.PageIndexRepository;
import es.bvalero.replacer.repository.PageModel;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@Qualifier("pageBatchIndexService")
class PageIndexBatchService extends PageIndexService implements PageIndexer {

    @Qualifier("pageIndexBatchRepository")
    @Autowired
    private PageIndexRepository pageIndexRepository;

    @Autowired
    private PageIndexResultBatchSaver pageIndexResultSaver;

    @Override
    protected Optional<PageModel> findByPageId(WikipediaPageId pageId) {
        return pageIndexRepository.findPageById(pageId);
    }

    @Override
    protected void saveResult(PageIndexResult result) {
        pageIndexResultSaver.save(result);
    }

    @Override
    public void finish() {
        pageIndexResultSaver.forceSave();
    }
}
