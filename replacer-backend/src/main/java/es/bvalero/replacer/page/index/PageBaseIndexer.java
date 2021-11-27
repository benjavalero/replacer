package es.bvalero.replacer.page.index;

import es.bvalero.replacer.common.domain.Replacement;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.common.domain.WikipediaPageId;
import es.bvalero.replacer.finder.FinderPageMapper;
import es.bvalero.replacer.finder.replacement.ReplacementFinderService;
import es.bvalero.replacer.finder.replacement.ReplacementMapper;
import es.bvalero.replacer.page.repository.PageModel;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;

@Slf4j
public abstract class PageBaseIndexer {

    @Autowired
    private PageIndexValidator pageIndexValidator;

    @Autowired
    private ReplacementFinderService replacementFinderService;

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

            PageIndexResult result = PageIndexHelper.indexPageReplacements(indexablePage, dbPage);
            saveResult(result);

            return result.withReplacements(replacements);
        } catch (Exception e) {
            // Just in case capture possible exceptions to continue indexing other pages
            LOGGER.error("Page not indexed: {}", page, e);
            return PageIndexResult.builder().status(PageIndexStatus.PAGE_NOT_INDEXED).build();
        }
    }

    @Nullable
    IndexablePage findIndexablePageInDb(WikipediaPageId pageId) {
        return findByPageId(pageId).map(IndexablePageMapper::fromModel).orElse(null);
    }

    abstract Optional<PageModel> findByPageId(WikipediaPageId pageId);

    private boolean isPageNotIndexable(WikipediaPage page, @Nullable IndexablePage dbPage) {
        // Check if the page is indexable (by namespace)
        // Redirection pages are now considered indexable but discarded when finding immutables
        try {
            pageIndexValidator.validateIndexable(page);
            return false;
        } catch (NonIndexablePageException e) {
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
        }
        return true;
    }

    private void indexObsoletePage(IndexablePage dbPage) {
        saveResult(PageIndexResult.builder().deletePages(Set.of(dbPage)).build());
    }

    abstract void saveResult(PageIndexResult result);

    private boolean isPageNotIndexed(WikipediaPage page, @Nullable IndexablePage dbPage) {
        // Check if the page (indexable by namespace) will be indexed
        return isNotIndexableByTimestamp(page, dbPage) && isNotIndexableByPageTitle(page, dbPage);
    }

    private boolean isNotIndexableByTimestamp(WikipediaPage page, @Nullable IndexablePage dbPage) {
        // If page modified in dump equals to the last indexing, always reindex.
        // If page modified in dump after last indexing, always reindex.
        // If page modified in dump before last indexing, do not index.
        LocalDate dbDate = Optional.ofNullable(dbPage).map(IndexablePage::getLastUpdate).orElse(null);
        if (dbDate == null) {
            return false;
        } else {
            return page.getLastUpdate().toLocalDate().isBefore(dbDate);
        }
    }

    private boolean isNotIndexableByPageTitle(WikipediaPage page, @Nullable IndexablePage dbPage) {
        // In case the page title has changed we force the page indexing
        String dbTitle = dbPage == null ? null : dbPage.getTitle();
        return Objects.equals(page.getTitle(), dbTitle);
    }

    private Collection<Replacement> findPageReplacements(WikipediaPage page) {
        return ReplacementMapper.toDomain(replacementFinderService.find(FinderPageMapper.fromDomain(page)));
    }

    public void indexObsoletePage(WikipediaPageId pageId) {
        IndexablePage page = findIndexablePageInDb(pageId);
        if (page != null) {
            indexObsoletePage(page);
        }
    }
}
