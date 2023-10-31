package es.bvalero.replacer.page.index;

import es.bvalero.replacer.finder.ReplacementFindService;
import es.bvalero.replacer.page.IndexedPage;
import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.page.PageService;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PageIndexService extends PageIndexAbstractService {

    // Dependency injection
    private final PageService pageService;
    private final PageComparatorSaver pageComparatorSaver;

    public PageIndexService(
        PageService pageService,
        PageIndexValidator pageIndexValidator,
        ReplacementFindService replacementFindService,
        PageComparator pageComparator,
        PageComparatorSaver pageComparatorSaver
    ) {
        super(pageService, pageIndexValidator, replacementFindService, pageComparator);
        this.pageService = pageService;
        this.pageComparatorSaver = pageComparatorSaver;
    }

    @Override
    Optional<IndexedPage> findIndexedPageByKey(PageKey pageKey) {
        return pageService.findPageByKey(pageKey);
    }

    @Override
    void saveResult(PageComparatorResult result) {
        pageComparatorSaver.save(result);
    }
}
