package es.bvalero.replacer.page.index;

import es.bvalero.replacer.common.domain.Suggestion;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.common.domain.WikipediaPageId;
import es.bvalero.replacer.finder.FinderPageMapper;
import es.bvalero.replacer.finder.replacement.Replacement;
import es.bvalero.replacer.finder.replacement.ReplacementFinderService;
import es.bvalero.replacer.finder.replacement.ReplacementSuggestion;
import es.bvalero.replacer.page.repository.PageModel;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
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
            IndexablePage dbPage = findIndexablePageInDb(page.getId()).orElse(null);

            validatePage(page, dbPage);

            Collection<es.bvalero.replacer.common.domain.Replacement> replacements = findPageReplacements(page);
            IndexablePage indexablePage = IndexablePageMapper.fromDomain(page, replacements);

            return indexPageReplacements(indexablePage, dbPage).withReplacements(replacements);
        } catch (NonIndexablePageException e) {
            return PageIndexResult.builder().status(PageIndexStatus.PAGE_NOT_INDEXABLE).build();
        } catch (Exception e) {
            // Just in case capture possible exceptions to continue indexing other pages
            LOGGER.error("Page not indexed: {}", page, e);
            return PageIndexResult.builder().status(PageIndexStatus.PAGE_NOT_INDEXED).build();
        }
    }

    private Collection<es.bvalero.replacer.common.domain.Replacement> findPageReplacements(WikipediaPage page) {
        return toDomain(replacementFinderService.find(FinderPageMapper.fromDomain(page)));
    }

    // TODO: Temporary while refactoring
    public static Collection<es.bvalero.replacer.common.domain.Replacement> toDomain(
        Collection<Replacement> replacements
    ) {
        return replacements.stream().map(PageBaseIndexer::toDomain).collect(Collectors.toUnmodifiableList());
    }

    // TODO: Temporary while refactoring
    private static es.bvalero.replacer.common.domain.Replacement toDomain(Replacement replacement) {
        return es.bvalero.replacer.common.domain.Replacement
            .builder()
            .start(replacement.getStart())
            .text(replacement.getText())
            .type(replacement.getType())
            .subtype(replacement.getSubtype())
            .suggestions(toDomainSuggestion(replacement.getSuggestions()))
            .build();
    }

    // TODO: Temporary while refactoring
    private static Collection<Suggestion> toDomainSuggestion(Collection<ReplacementSuggestion> suggestions) {
        return suggestions.stream().map(PageBaseIndexer::toDomainSuggestion).collect(Collectors.toUnmodifiableList());
    }

    // TODO: Temporary while refactoring
    private static Suggestion toDomainSuggestion(ReplacementSuggestion suggestion) {
        return Suggestion.of(suggestion.getText(), suggestion.getComment());
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
                indexObsoletePage(dbPage);
            }
            throw e;
        }
    }

    Optional<IndexablePage> findIndexablePageInDb(WikipediaPageId pageId) {
        return findByPageId(pageId).map(IndexablePageMapper::fromModel);
    }

    abstract Optional<PageModel> findByPageId(WikipediaPageId pageId);

    private PageIndexResult indexPageReplacements(IndexablePage page, @Nullable IndexablePage dbPage) {
        // The page is not indexed in case the last-update in database is later than the last-update of the given page
        if (isNotIndexable(page, dbPage)) {
            return PageIndexResult.ofEmpty();
        }

        PageIndexResult result = PageIndexHelper.indexPageReplacements(page, dbPage);
        saveResult(result);

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

    abstract void saveResult(PageIndexResult result);

    public void indexObsoletePage(WikipediaPageId pageId) {
        findIndexablePageInDb(pageId).ifPresent(this::indexObsoletePage);
    }

    private void indexObsoletePage(IndexablePage dbPage) {
        saveResult(PageIndexResult.builder().deletePages(Set.of(dbPage)).build());
    }
}
