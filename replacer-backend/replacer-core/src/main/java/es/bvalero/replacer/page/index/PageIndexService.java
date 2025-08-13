package es.bvalero.replacer.page.index;

import es.bvalero.replacer.common.domain.PageKey;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.ReplacementFindApi;
import es.bvalero.replacer.finder.StandardType;
import es.bvalero.replacer.page.IndexedPage;
import es.bvalero.replacer.page.find.PageRepository;
import es.bvalero.replacer.page.save.PageSaveRepository;
import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import es.bvalero.replacer.wikipedia.WikipediaPageRepository;
import es.bvalero.replacer.wikipedia.WikipediaSearchRequest;
import java.util.Collection;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class PageIndexService extends PageIndexAbstractService {

    // Dependency injection
    private final WikipediaPageRepository wikipediaPageRepository;
    private final PageRepository pageRepository;
    private final PageComparatorSaver pageComparatorSaver;

    public PageIndexService(
        WikipediaPageRepository wikipediaPageRepository,
        PageSaveRepository pageSaveRepository,
        PageIndexValidator pageIndexValidator,
        ReplacementFindApi replacementFindApi,
        PageComparator pageComparator,
        PageRepository pageRepository,
        PageComparatorSaver pageComparatorSaver
    ) {
        super(pageSaveRepository, pageIndexValidator, replacementFindApi, pageComparator);
        this.wikipediaPageRepository = wikipediaPageRepository;
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

    public PageIndexResult indexPage(WikipediaPage wikipediaPage) {
        return indexPage(IndexablePage.of(wikipediaPage));
    }

    public void indexType(WikipediaLanguage lang, StandardType type) {
        // For each type, find the Wikipedia pages containing the text, using the default search options.
        // At least some of them not to overload the Wikipedia search.
        WikipediaSearchRequest request = WikipediaSearchRequest.builder().lang(lang).text(type.getSubtype()).build();
        Collection<PageKey> pageKeys = wikipediaPageRepository
            .findByContent(request)
            .getPageIds()
            .stream()
            .map(pageId -> PageKey.of(lang, pageId))
            .toList();
        try {
            wikipediaPageRepository.findByKeys(pageKeys).forEach(this::indexPage);
        } catch (WikipediaException e) {
            // Do nothing, simply we don't index in case we cannot retrieve the pages.
        }
    }
}
