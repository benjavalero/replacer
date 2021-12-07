package es.bvalero.replacer.page.index;

import es.bvalero.replacer.common.domain.Replacement;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.common.domain.WikipediaPageId;
import es.bvalero.replacer.finder.FinderPageMapper;
import es.bvalero.replacer.finder.replacement.ReplacementFinderService;
import es.bvalero.replacer.finder.replacement.ReplacementMapper;
import es.bvalero.replacer.repository.PageModel;
import es.bvalero.replacer.repository.PageRepository;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/** Index a page: calculate its replacements and update the related replacements in DB accordingly */
@Slf4j
@Component
public class PageIndexer {

    @Autowired
    private PageRepository pageRepository;

    @Autowired
    private PageIndexValidator pageIndexValidator;

    @Autowired
    private ReplacementFinderService replacementFinderService;

    @Autowired
    private IndexablePageComparator indexablePageComparator;

    @Autowired
    private PageIndexResultSaver pageIndexResultSaver;

    /** Index a page. Replacements and details in database (if any) will be calculated. */
    public PageIndexResult indexPageReplacements(WikipediaPage page) {
        try {
            IndexablePage dbPage = findIndexablePageInDb(page.getId());

            // Check if the page is indexable by itself
            if (isPageNotIndexable(page, dbPage)) {
                return PageIndexResult.builder().status(PageIndexStatus.PAGE_NOT_INDEXABLE).build();
            } else if (isPageNotIndexed(page, dbPage)) {
                return PageIndexResult.builder().status(PageIndexStatus.PAGE_NOT_INDEXED).build();
            }

            Collection<Replacement> replacements = findPageReplacements(page);
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

    protected Optional<PageModel> findByPageId(WikipediaPageId pageId) {
        return pageRepository.findPageById(pageId);
    }

    private boolean isPageNotIndexable(WikipediaPage page, @Nullable IndexablePage dbPage) {
        // Check if the page is indexable (by namespace)
        // Redirection pages are now considered indexable but discarded when finding immutables
        if (pageIndexValidator.isPageIndexableByNamespace(page)) {
            return false;
        } else {
            // If the page is not indexable then it should not exist in DB
            if (dbPage != null) {
                LOGGER.error(
                    "Unexpected page in DB not indexable: {} - {} - {}",
                    page.getId().getLang(),
                    page.getTitle(),
                    dbPage.getTitle()
                );
                indexObsoletePage(dbPage);
            }
            return true;
        }
    }

    private void indexObsoletePage(IndexablePage dbPage) {
        saveResult(PageIndexResult.builder().removePages(Set.of(dbPage)).build());
    }

    protected void saveResult(PageIndexResult result) {
        pageIndexResultSaver.save(result);
    }

    private boolean isPageNotIndexed(WikipediaPage page, @Nullable IndexablePage dbPage) {
        // Check if the page (indexable by namespace) will be indexed
        return (
            !pageIndexValidator.isIndexableByTimestamp(page, dbPage) &&
            !pageIndexValidator.isIndexableByPageTitle(page, dbPage)
        );
    }

    private Collection<Replacement> findPageReplacements(WikipediaPage page) {
        return ReplacementMapper.toDomain(replacementFinderService.find(FinderPageMapper.fromDomain(page)));
    }

    /** Index a page which should not be in database because it has been deleted or is not indexable anymore */
    public void indexObsoletePage(WikipediaPageId pageId) {
        IndexablePage page = findIndexablePageInDb(pageId);
        if (page != null) {
            this.indexObsoletePage(page);
        }
    }
}
