package es.bvalero.replacer.page.index;

import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.ReplacementFindService;
import es.bvalero.replacer.page.IndexedPage;
import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.page.PageService;
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
    private PageService pageService;

    @Autowired
    private PageIndexValidator pageIndexValidator;

    @Autowired
    private ReplacementFindService replacementFindService;

    @Autowired
    private PageComparator pageComparator;

    /** Index a page. Replacements and details in database (if any) will be calculated. */
    public PageIndexResult indexPage(IndexablePage page) {
        try {
            final IndexedPage dbPage = findIndexedPage(page.getPageKey());

            // Consider as "indexable" all pages belonging to the configured namespaces
            if (!isPageIndexable(page)) {
                removeObsoletePage(dbPage);
                return PageIndexResult.ofNotIndexable();
            }

            return indexPage(page, dbPage);
        } catch (Exception e) {
            // Just in case capture possible exceptions to continue indexing other pages
            LOGGER.error("Page not indexed: {}", page, e);
            return PageIndexResult.ofNotIndexable();
        }
    }

    @Nullable
    private IndexedPage findIndexedPage(PageKey pageKey) {
        return findIndexedPageByKey(pageKey).orElse(null);
    }

    // This method can be overridden in case we want to avoid calculating the replacements under some circumstances
    PageIndexResult indexPage(IndexablePage indexablePage, @Nullable IndexedPage dbPage) {
        final Collection<Replacement> replacements = replacementFindService.findReplacements(indexablePage);

        final PageComparatorResult result = pageComparator.indexPageReplacements(indexablePage, replacements, dbPage);
        if (!result.isEmpty()) {
            saveResult(result);
        }

        return PageIndexResult.of(
            result.isEmpty() ? PageIndexStatus.PAGE_NOT_INDEXED : PageIndexStatus.PAGE_INDEXED,
            result.getReplacementsToReview()
        );
    }

    abstract Optional<IndexedPage> findIndexedPageByKey(PageKey pageKey);

    private boolean isPageIndexable(IndexablePage page) {
        // Only check if the page is indexable by namespace
        // Redirection pages should be detected before analyzing content
        // but just in case they are discarded when finding immutables
        return pageIndexValidator.isPageIndexableByNamespace(page) && !page.isRedirect();
    }

    private void removeObsoletePage(@Nullable IndexedPage dbPage) {
        // Just in case the page already exists in database but is not indexable anymore
        if (dbPage != null) {
            LOGGER.warn(
                "Unexpected page in DB not indexable: {} - {}",
                dbPage.getPageKey().getLang(),
                dbPage.getTitle()
            );
            pageService.removePagesByKey(Collections.singleton(dbPage.getPageKey()));
        }
    }

    abstract void saveResult(PageComparatorResult result);
}
