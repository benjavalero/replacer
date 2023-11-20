package es.bvalero.replacer.page.index;

import es.bvalero.replacer.common.util.ReplacerUtils;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.ReplacementFindService;
import es.bvalero.replacer.page.IndexedPage;
import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.page.PageRepository;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

/** Index a page: calculate its replacements and update the related replacements in DB accordingly */
@Slf4j
@Service
abstract class PageIndexAbstractService {

    // Dependency injection
    private final PageRepository pageRepository;
    private final PageIndexValidator pageIndexValidator;
    private final ReplacementFindService replacementFindService;
    private final PageComparator pageComparator;

    PageIndexAbstractService(
        PageRepository pageRepository,
        PageIndexValidator pageIndexValidator,
        ReplacementFindService replacementFindService,
        PageComparator pageComparator
    ) {
        this.pageRepository = pageRepository;
        this.pageIndexValidator = pageIndexValidator;
        this.replacementFindService = replacementFindService;
        this.pageComparator = pageComparator;
    }

    /** Index a page. Replacements and details in database (if any) will be calculated. */
    public PageIndexResult indexPage(IndexablePage page) {
        final IndexedPage dbPage = findIndexedPage(page.getPageKey());

        // Consider as "indexable" all pages belonging to the configured namespaces
        if (!isPageIndexable(page)) {
            if (dbPage != null) {
                // Just in case the page already exists in database but is not indexable anymore
                removeObsoletePage(page, dbPage);
            }
            return PageIndexResult.ofNotIndexable();
        }

        try {
            return indexPage(page, dbPage);
        } catch (Exception e) {
            // Just in case capture possible exceptions to continue indexing other pages
            LOGGER.error("Error indexing page: {}", page, e);
            return PageIndexResult.ofNotIndexed();
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

    private void removeObsoletePage(IndexablePage page, IndexedPage dbPage) {
        assert page.getPageKey().equals(dbPage.getPageKey());
        LOGGER.warn(
            "Page in DB is not indexable anymore: {}",
            ReplacerUtils.toJson(
                "lang",
                page.getPageKey().getLang(),
                "pageId",
                page.getPageKey().getPageId(),
                "namespace",
                page.getNamespace(),
                "title",
                page.getTitle(),
                "dbTitle",
                dbPage.getTitle(),
                "redirect",
                page.isRedirect()
            )
        );
        pageRepository.removeByKey(Set.of(page.getPageKey()));
    }

    abstract void saveResult(PageComparatorResult result);
}
