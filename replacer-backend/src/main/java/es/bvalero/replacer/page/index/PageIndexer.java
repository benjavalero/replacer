package es.bvalero.replacer.page.index;

import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.common.domain.WikipediaPageId;
import es.bvalero.replacer.finder.FinderPageMapper;
import es.bvalero.replacer.finder.replacement.Replacement;
import es.bvalero.replacer.finder.replacement.ReplacementFinderService;
import es.bvalero.replacer.page.repository.PageRepository;
import java.time.LocalDate;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/** Index a page: calculate its replacements and update the related replacements in DB accordingly */
@Slf4j
@Component
public class PageIndexer {

    @Autowired
    private PageIndexValidator pageIndexValidator;

    @Autowired
    private PageRepository pageRepository;

    @Autowired
    private ReplacementFinderService replacementFinderService;

    @Autowired
    private PageIndexResultSaver pageIndexResultSaver;

    /**
     * Index a page against its details in database (if any). Current replacements will be calculated.
     * Returns if changes are performed in database due to the page indexing.
     * Throws a custom exception in case the page is not processable.
     */
    public boolean indexPageReplacements(WikipediaPage page, @Nullable IndexablePage dbPage)
        throws PageNotProcessableException {
        validatePage(page, dbPage);

        List<Replacement> replacements = replacementFinderService.find(FinderPageMapper.fromDomain(page));
        return indexPageReplacements(IndexablePageMapper.fromDomain(page, replacements), dbPage, true);
    }

    /** Index a page and its replacements. Details in database (if any) will be calculated. */
    public void indexPageReplacements(WikipediaPage page, Collection<Replacement> replacements)
        throws PageNotProcessableException {
        IndexablePage indexablePage = IndexablePageMapper.fromDomain(page, replacements);
        IndexablePage dbPage = findIndexablePageInDb(page.getId()).orElse(null);

        validatePage(page, dbPage);
        indexPageReplacements(indexablePage, dbPage, false);
    }

    private void validatePage(WikipediaPage page, @Nullable IndexablePage dbPage) throws PageNotProcessableException {
        // Check if it is processable (by namespace)
        // Redirection pages are now considered processable but discarded when finding immutables
        try {
            pageIndexValidator.validateProcessable(page);
        } catch (PageNotProcessableException e) {
            // If the page is not processable then it should not exist in DB
            if (dbPage != null) {
                LOGGER.error(
                    "Unexpected page in DB not processable: {} - {} - {}",
                    page.getId().getLang(),
                    page.getTitle(),
                    dbPage.getTitle()
                );
                indexObsoletePage(dbPage, true);
            }
            throw e;
        }
    }

    private Optional<IndexablePage> findIndexablePageInDb(WikipediaPageId pageId) {
        return pageRepository.findByPageId(pageId).map(IndexablePageMapper::fromModel);
    }

    private boolean indexPageReplacements(IndexablePage page, @Nullable IndexablePage dbPage, boolean batchSave) {
        // The page is not indexed in case the last-update in database is later than the last-update of the given page
        if (isNotProcessable(page, dbPage)) {
            return false;
        }

        PageIndexResult result = PageIndexHelper.indexPageReplacements(page, dbPage);
        saveResult(result, batchSave);

        // Return if the page has been processed, i.e. modifications have been applied in database.
        return !result.isEmpty();
    }

    private boolean isNotProcessable(IndexablePage page, @Nullable IndexablePage dbPage) {
        return isNotProcessableByTimestamp(page, dbPage) && isNotProcessableByPageTitle(page, dbPage);
    }

    private boolean isNotProcessableByTimestamp(IndexablePage page, @Nullable IndexablePage dbPage) {
        // If page modified in dump equals to the last indexing, reprocess always.
        // If page modified in dump after last indexing, reprocess always.
        // If page modified in dump before last indexing, do not reprocess.
        LocalDate dbDate = Optional.ofNullable(dbPage).map(IndexablePage::getLastUpdate).orElse(null);
        if (page.getLastUpdate() == null || dbDate == null) {
            return false;
        } else {
            return Objects.requireNonNull(page.getLastUpdate()).isBefore(dbDate);
        }
    }

    private boolean isNotProcessableByPageTitle(IndexablePage page, @Nullable IndexablePage dbPage) {
        // In case the page title has changed we force the page processing
        String dbTitle = dbPage == null ? null : dbPage.getTitle();
        return Objects.equals(page.getTitle(), dbTitle);
    }

    private void saveResult(PageIndexResult result, boolean batchSave) {
        if (!result.isEmpty()) {
            if (batchSave) {
                pageIndexResultSaver.saveBatch(result);
            } else {
                pageIndexResultSaver.save(result);
            }
        }
    }

    /** Index a page which should not be in database because it has been deleted or is not processable anymore */
    public void indexObsoletePage(WikipediaPageId pageId) {
        findIndexablePageInDb(pageId).ifPresent(page -> indexObsoletePage(page, false));
    }

    private void indexObsoletePage(IndexablePage dbPage, boolean batchSave) {
        saveResult(PageIndexResult.builder().deletePages(Set.of(dbPage)).build(), batchSave);
    }

    /* Force saving what is left on the batch */
    public void forceSave() {
        pageIndexResultSaver.forceSave();
    }
}
