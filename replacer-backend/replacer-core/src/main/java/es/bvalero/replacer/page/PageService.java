package es.bvalero.replacer.page;

import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service to manage the persistence of the pages, especially after indexing or to find review candidates.
 * All methods are grouped here as a facade to the page repository.
 */
@Service
public class PageService {

    @Autowired
    private PageRepository pageRepository;

    public Optional<IndexedPage> findPageByKey(PageKey pageKey) {
        return pageRepository.findPageByKey(pageKey);
    }

    public void addPages(Collection<IndexedPage> pages) {
        pageRepository.add(pages);
    }

    public void updatePages(Collection<IndexedPage> pages) {
        pageRepository.update(pages);
    }

    public void updatePageLastUpdate(PageKey pageKey, LocalDate lastUpdate) {
        pageRepository.updateLastUpdate(pageKey, lastUpdate);
    }

    public void removePagesByKey(Collection<PageKey> pageKeys) {
        pageRepository.removeByKey(pageKeys);
    }

    public Collection<PageKey> findPagesToReviewByNoType(WikipediaLanguage lang, int numResults) {
        return pageRepository.findNotReviewed(lang, null, numResults);
    }

    public int countPagesToReviewByNoType(WikipediaLanguage lang) {
        return pageRepository.countNotReviewedByType(lang, null);
    }

    public Collection<PageKey> findPagesToReviewByType(WikipediaLanguage lang, ReplacementType type, int numResults) {
        return pageRepository.findNotReviewed(lang, type, numResults);
    }

    public int countPagesToReviewByType(WikipediaLanguage lang, ReplacementType type) {
        return pageRepository.countNotReviewedByType(lang, type);
    }
}