package es.bvalero.replacer.page.index;

import es.bvalero.replacer.common.domain.WikipediaPageId;
import es.bvalero.replacer.repository.PageIndexRepository;
import es.bvalero.replacer.repository.PageModel;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Slf4j
@Primary
@Service
class PageIndexSingleService extends PageIndexAbstractService implements PageIndexService {

    @Autowired
    private PageIndexRepository pageIndexRepository;

    @Autowired
    private PageIndexResultSaver pageIndexResultSaver;

    @Override
    Optional<PageModel> findByPageId(WikipediaPageId pageId) {
        return pageIndexRepository.findPageById(pageId);
    }

    @Override
    void saveResult(PageIndexResult result) {
        pageIndexResultSaver.save(result);
    }

    @Override
    public void finish() {
        // Do nothing
    }
}
