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

            // 1. Consider as "indexable" all pages belonging to the configured namespaces
            // 2. Consider as "not indexed" all indexable pages which are not worth to be re-indexed
            // because they have already been indexed recently in the database
            if (!isPageIndexable(page, dbPage)) {
                return PageIndexResult.builder().status(PageIndexStatus.PAGE_NOT_INDEXABLE).build();
            } else if (!isPageToBeIndexed(page, dbPage)) {
                return PageIndexResult.builder().status(PageIndexStatus.PAGE_NOT_INDEXED).build();
            }

            Collection<PageReplacement> replacements = findPageReplacements(page);
            IndexablePage indexablePage = IndexablePageMapper.fromDomain(page, replacements);

            PageIndexResult result = indexablePageComparator.indexPageReplacements(indexablePage, dbPage);
            saveResult(result);

            return result.withReplacements(replacements);
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

    abstract Optional<PageModel> findByPageId(WikipediaPageId pageId);

    private boolean isPageIndexable(WikipediaPage page, @Nullable IndexablePage dbPage) {
        // Only check if the page is indexable by namespace
        // Redirection pages are now considered indexable but discarded when finding immutables
        if (pageIndexValidator.isPageIndexableByNamespace(page)) {
            return true;
        } else {
            // Just in case the page already exists in database but is not indexable anymore
            if (dbPage != null) {
                LOGGER.error(
                    "Unexpected page in DB not indexable: {} - {} - {}",
                    page.getId().getLang(),
                    page.getTitle(),
                    dbPage.getTitle()
                );
                removeObsoletePage(page);
            }
            return false;
        }
    }

    private void removeObsoletePage(WikipediaPage page) {
        removeObsoletePageService.removeObsoletePages(Collections.singleton(page.getId()));
    }

    abstract void saveResult(PageIndexResult result);

    private boolean isPageToBeIndexed(WikipediaPage page, @Nullable IndexablePage dbPage) {
        // We assume at this point that the page is indexable
        // Check if the page will be re-indexed (by timestamp)
        // Page will also be indexed in case the title is not aligned
        return (
            pageIndexValidator.isIndexableByTimestamp(page, dbPage) ||
            pageIndexValidator.isIndexableByPageTitle(page, dbPage)
        );
    }

    private Collection<PageReplacement> findPageReplacements(WikipediaPage page) {
        return findReplacementsService.findReplacements(page);
    }
}
