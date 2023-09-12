package es.bvalero.replacer.index;

import es.bvalero.replacer.page.IndexedPage;
import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.page.PageService;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PageIndexService extends PageIndexAbstractService {

    @Autowired
    private PageService pageService;

    @Autowired
    private PageComparatorSaver pageComparatorSaver;

    @Override
    Optional<IndexedPage> findIndexedPageByKey(PageKey pageKey) {
        return pageService.findPageByKey(pageKey);
    }

    @Override
    void saveResult(PageComparatorResult result) {
        pageComparatorSaver.save(result);
    }
}
