package es.bvalero.replacer.page.index;

import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.common.domain.WikipediaPageId;
import es.bvalero.replacer.finder.FinderPageMapper;
import es.bvalero.replacer.finder.replacement.Replacement;
import es.bvalero.replacer.finder.replacement.ReplacementFinderService;
import es.bvalero.replacer.finder.replacement.ReplacementSuggestion;
import es.bvalero.replacer.page.repository.PageRepository;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/** Index a page: calculate its replacements and update the related replacements in DB accordingly */
@Slf4j
@Component
public class PageIndexer {

    @Autowired
    private PageIndexValidator pageIndexValidator;

    @Autowired
    @Qualifier("pageCacheRepository")
    private PageRepository pageCacheRepository;

    @Autowired
    @Qualifier("pageJdbcRepository")
    private PageRepository pageRepository;

    @Autowired
    private ReplacementFinderService replacementFinderService;

    @Autowired
    private PageIndexResultSaver pageIndexResultSaver;

    /** Index a page. Replacements and details in database (if any) will be calculated. */
    public PageIndexResult indexPageReplacements(WikipediaPage page) {
        return indexPageReplacements(page, false);
    }

    /** Index a page. Replacements and details in database (if any) will be calculated. */
    public PageIndexResult indexPageReplacementsInBatch(WikipediaPage page) {
        return indexPageReplacements(page, true);
    }

    private PageIndexResult indexPageReplacements(WikipediaPage page, boolean batchSave) {
        IndexablePage dbPage = findIndexablePageInDb(page.getId(), batchSave).orElse(null);

        try {
            validatePage(page, dbPage);
        } catch (NonIndexablePageException e) {
            return PageIndexResult.builder().status(PageIndexStatus.PAGE_NOT_INDEXABLE).build();
        }

        Collection<es.bvalero.replacer.common.domain.Replacement> replacements = findPageReplacements(page);
        IndexablePage indexablePage = IndexablePageMapper.fromDomain(page, replacements);

        return indexPageReplacements(indexablePage, dbPage, batchSave).withReplacements(replacements);
    }

    private Collection<es.bvalero.replacer.common.domain.Replacement> findPageReplacements(WikipediaPage page) {
        return toDomain(replacementFinderService.find(FinderPageMapper.fromDomain(page)), page);
    }

    // TODO: Temporary while refactoring
    public static Collection<es.bvalero.replacer.common.domain.Replacement> toDomain(
        Collection<Replacement> replacements,
        WikipediaPage page
    ) {
        return replacements.stream().map(r -> toDomain(r, page)).collect(Collectors.toUnmodifiableList());
    }

    // TODO: Temporary while refactoring
    private static es.bvalero.replacer.common.domain.Replacement toDomain(Replacement replacement, WikipediaPage page) {
        return es.bvalero.replacer.common.domain.Replacement
            .builder()
            .start(replacement.getStart())
            .text(replacement.getText())
            .type(replacement.getType())
            .subtype(replacement.getSubtype())
            .context(replacement.getContext(page.getContent()))
            .suggestions(toDomainSuggestion(replacement.getSuggestions()))
            .build();
    }

    // TODO: Temporary while refactoring
    private static Collection<es.bvalero.replacer.common.domain.ReplacementSuggestion> toDomainSuggestion(
        Collection<ReplacementSuggestion> suggestions
    ) {
        return suggestions.stream().map(PageIndexer::toDomainSuggestion).collect(Collectors.toUnmodifiableList());
    }

    // TODO: Temporary while refactoring
    private static es.bvalero.replacer.common.domain.ReplacementSuggestion toDomainSuggestion(
        ReplacementSuggestion suggestion
    ) {
        return es.bvalero.replacer.common.domain.ReplacementSuggestion.of(
            suggestion.getText(),
            suggestion.getComment()
        );
    }

    private void validatePage(WikipediaPage page, @Nullable IndexablePage dbPage) throws NonIndexablePageException {
        // Check if it is indexable (by namespace)
        // Redirection pages are now considered indexable but discarded when finding immutables
        try {
            pageIndexValidator.validateIndexable(page);
        } catch (NonIndexablePageException e) {
            // If the page is not indexable then it should not exist in DB
            if (dbPage != null) {
                LOGGER.error(
                    "Unexpected page in DB not indexable: {} - {} - {}",
                    page.getId().getLang(),
                    page.getTitle(),
                    dbPage.getTitle()
                );
                indexObsoletePage(dbPage, true);
            }
            throw e;
        }
    }

    private Optional<IndexablePage> findIndexablePageInDb(WikipediaPageId pageId, boolean batchSave) {
        PageRepository repository = batchSave ? pageCacheRepository : pageRepository;
        return repository.findByPageId(pageId).map(IndexablePageMapper::fromModel);
    }

    private PageIndexResult indexPageReplacements(
        IndexablePage page,
        @Nullable IndexablePage dbPage,
        boolean batchSave
    ) {
        // The page is not indexed in case the last-update in database is later than the last-update of the given page
        if (isNotIndexable(page, dbPage)) {
            return PageIndexResult.ofEmpty();
        }

        PageIndexResult result = PageIndexHelper.indexPageReplacements(page, dbPage);
        saveResult(result, batchSave);

        // Return if the page has been indexed, i.e. modifications have been applied in database.
        return result;
    }

    private boolean isNotIndexable(IndexablePage page, @Nullable IndexablePage dbPage) {
        return isNotIndexableByTimestamp(page, dbPage) && isNotIndexableByPageTitle(page, dbPage);
    }

    private boolean isNotIndexableByTimestamp(IndexablePage page, @Nullable IndexablePage dbPage) {
        // If page modified in dump equals to the last indexing, always reindex.
        // If page modified in dump after last indexing, always reindex.
        // If page modified in dump before last indexing, do not index.
        LocalDate dbDate = Optional.ofNullable(dbPage).map(IndexablePage::getLastUpdate).orElse(null);
        if (page.getLastUpdate() == null || dbDate == null) {
            return false;
        } else {
            return Objects.requireNonNull(page.getLastUpdate()).isBefore(dbDate);
        }
    }

    private boolean isNotIndexableByPageTitle(IndexablePage page, @Nullable IndexablePage dbPage) {
        // In case the page title has changed we force the page indexing
        String dbTitle = dbPage == null ? null : dbPage.getTitle();
        return Objects.equals(page.getTitle(), dbTitle);
    }

    private void saveResult(PageIndexResult result, boolean batchSave) {
        if (result.isNotEmpty()) {
            if (batchSave) {
                pageIndexResultSaver.saveBatch(result);
            } else {
                pageIndexResultSaver.save(result);
            }
        }
    }

    /** Index a page which should not be in database because it has been deleted or is not indexable anymore */
    public void indexObsoletePage(WikipediaPageId pageId) {
        findIndexablePageInDb(pageId, false).ifPresent(page -> indexObsoletePage(page, false));
    }

    private void indexObsoletePage(IndexablePage dbPage, boolean batchSave) {
        saveResult(PageIndexResult.builder().deletePages(Set.of(dbPage)).build(), batchSave);
    }

    /* Force saving what is left on the batch */
    public void forceSave() {
        pageIndexResultSaver.forceSave();
        pageCacheRepository.resetCache();
    }
}
