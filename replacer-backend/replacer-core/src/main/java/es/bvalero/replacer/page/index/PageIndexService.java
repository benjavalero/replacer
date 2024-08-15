package es.bvalero.replacer.page.index;

import es.bvalero.replacer.page.IndexedPage;
import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.page.PageRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class PageIndexService extends PageIndexAbstractService {

    // Dependency injection
    private final PageRepository pageRepository;
    private final PageComparatorSaver pageComparatorSaver;

    public PageIndexService(
        PageRepository pageRepository,
        PageIndexValidator pageIndexValidator,
        ReplacementFindService replacementFindService,
        PageComparator pageComparator,
        PageComparatorSaver pageComparatorSaver
    ) {
        super(pageRepository, pageIndexValidator, replacementFindService, pageComparator);
        this.pageRepository = pageRepository;
        this.pageComparatorSaver = pageComparatorSaver;
    }

    @Override
    Optional<IndexedPage> findIndexedPageByKey(PageKey pageKey) {
        return pageRepository.findByKey(pageKey);
    }

    @Override
    void saveResult(PageComparatorResult result) {
        pageComparatorSaver.save(result);
    }
}
