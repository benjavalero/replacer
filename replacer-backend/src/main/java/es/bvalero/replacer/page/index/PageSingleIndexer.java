package es.bvalero.replacer.page.index;

import es.bvalero.replacer.common.domain.WikipediaPageId;
import es.bvalero.replacer.page.repository.PageModel;
import es.bvalero.replacer.page.repository.PageRepository;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
@Qualifier("pageSingleIndexer")
class PageSingleIndexer extends PageBaseIndexer implements PageIndexer {

    @Autowired
    @Qualifier("pageJdbcRepository")
    private PageRepository pageRepository;

    @Autowired
    private PageIndexResultSaver pageIndexResultSaver;

    @Override
    Optional<PageModel> findByPageId(WikipediaPageId pageId) {
        return pageRepository.findByPageId(pageId);
    }

    @Override
    void saveResult(PageIndexResult result) {
        pageIndexResultSaver.save(result);
    }

    @Override
    public void forceSave() {
        throw new IllegalCallerException();
    }
}
