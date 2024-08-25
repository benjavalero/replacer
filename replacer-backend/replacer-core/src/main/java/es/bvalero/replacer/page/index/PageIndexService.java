package es.bvalero.replacer.page.index;

import es.bvalero.replacer.page.IndexedPage;
import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.page.find.PageRepository;
import es.bvalero.replacer.page.save.PageSaveRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class PageIndexService extends PageIndexAbstractService {

    // Dependency injection
    private final PageRepository pageRepository;
    private final PageComparatorSaver pageComparatorSaver;

    public PageIndexService(
        PageSaveRepository pageSaveRepository,
        PageIndexValidator pageIndexValidator,
        ReplacementFindService replacementFindService,
        PageComparator pageComparator,
        PageRepository pageRepository,
        PageComparatorSaver pageComparatorSaver
    ) {
        super(pageSaveRepository, pageIndexValidator, replacementFindService, pageComparator);
        this.pageRepository = pageRepository;
        this.pageComparatorSaver = pageComparatorSaver;
    }

    @Override
    Optional<IndexedPage> findIndexedPageByKey(PageKey pageKey) {
        return pageRepository.findByKey(pageKey);
    }

    @Override
    void saveResult(IndexedPage indexedPage) {
        pageComparatorSaver.save(indexedPage);
    }
}
