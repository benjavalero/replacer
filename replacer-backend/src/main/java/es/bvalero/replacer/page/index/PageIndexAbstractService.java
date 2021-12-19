package es.bvalero.replacer.page.index;

import es.bvalero.replacer.common.domain.PageReplacement;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.common.domain.WikipediaPageId;
import es.bvalero.replacer.page.findreplacement.FindReplacementsService;
import es.bvalero.replacer.page.removeobsolete.RemoveObsoletePageService;
import es.bvalero.replacer.repository.PageModel;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

/** Index a page: calculate its replacements and update the related replacements in DB accordingly */
@Slf4j
@Service
abstract class PageIndexAbstractService {

    @Autowired
    private RemoveObsoletePageService removeObsoletePageService;

    @Autowired
    private PageIndexValidator pageIndexValidator;

    @Autowired
    private FindReplacementsService findReplacementsService;

    @Autowired
    private IndexablePageComparator indexablePageComparator;

    public PageIndexResult indexPage(WikipediaPage page) {
        try {
            IndexablePage dbPage = findIndexablePageInDb(page.getId());

            // Consider as "indexable" all pages belonging to the configured namespaces
            if (!isPageIndexable(page)) {
                removeObsoletePage(dbPage);
                return PageIndexResult.builder().status(PageIndexStatus.PAGE_NOT_INDEXABLE).build();
            }

            return indexPage(page, dbPage);
        } catch (Exception e) {
            // Just in case capture possible exceptions to continue indexing other pages
            LOGGER.error("Page not indexed: {}", page, e);
            return PageIndexResult.builder().status(PageIndexStatus.PAGE_NOT_INDEXED).build();
        }
    }

    @Nullable
    private IndexablePage findIndexablePageInDb(WikipediaPageId pageId) {
        return findByPageId(pageId).map(IndexablePageMapper::fromModel).orElse(null);
    }

    // This method can be overridden in case we want to avoid calculating the replacements under some circumstances
    PageIndexResult indexPage(WikipediaPage page, @Nullable IndexablePage dbPage) {
        Collection<PageReplacement> replacements = findPageReplacements(page);
        IndexablePage indexablePage = IndexablePageMapper.fromDomain(page, replacements);

        PageIndexResult result = indexablePageComparator.indexPageReplacements(indexablePage, dbPage);
        saveResult(result);

        return result.withReplacements(replacements);
    }

    abstract Optional<PageModel> findByPageId(WikipediaPageId pageId);

    private boolean isPageIndexable(WikipediaPage page) {
        // Only check if the page is indexable by namespace
        // Redirection pages are now considered indexable but discarded when finding immutables
        return pageIndexValidator.isPageIndexableByNamespace(page);
    }

    private void removeObsoletePage(@Nullable IndexablePage dbPage) {
        // Just in case the page already exists in database but is not indexable anymore
        if (dbPage != null) {
            LOGGER.error("Unexpected page in DB not indexable: {} - {}", dbPage.getId().getLang(), dbPage.getTitle());
            removeObsoletePageService.removeObsoletePages(
                Collections.singleton(IndexablePageMapper.toDomain(dbPage.getId()))
            );
        }
    }

    abstract void saveResult(PageIndexResult result);

    private Collection<PageReplacement> findPageReplacements(WikipediaPage page) {
        return findReplacementsService.findReplacements(page);
    }
}
