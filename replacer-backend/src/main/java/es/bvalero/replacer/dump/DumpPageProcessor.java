package es.bvalero.replacer.dump;

import com.jcabi.aspects.Loggable;
import es.bvalero.replacer.domain.ReplacerException;
import es.bvalero.replacer.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.replacement.Replacement;
import es.bvalero.replacer.finder.replacement.ReplacementFinderService;
import es.bvalero.replacer.page.index.PageIndexHelper;
import es.bvalero.replacer.page.index.PageIndexResult;
import es.bvalero.replacer.page.index.PageIndexResultSaver;
import es.bvalero.replacer.page.repository.IndexablePage;
import es.bvalero.replacer.page.repository.IndexablePageId;
import es.bvalero.replacer.page.repository.IndexablePageRepository;
import es.bvalero.replacer.page.repository.IndexableReplacement;
import es.bvalero.replacer.page.validate.PageValidator;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Process a page found in a Wikipedia dump.
 * It involves checking if the page needs to be indexed, finding the replacements in the page text
 * and finally index the replacements in the repository.
 */
@Slf4j
@Component
class DumpPageProcessor {

    @Autowired
    private PageValidator pageValidator;

    @Autowired
    @Qualifier("indexablePageCacheRepository")
    private IndexablePageRepository indexablePageRepository;

    @Autowired
    private PageIndexResultSaver pageIndexResultSaver;

    @Autowired
    private ReplacementFinderService replacementFinderService;

    @Autowired
    private PageIndexHelper pageIndexHelper;

    @Loggable(prepend = true, value = Loggable.TRACE)
    DumpPageProcessorResult process(DumpPage dumpPage) {
        // In all cases we find the current status of the page in the DB
        Optional<IndexablePage> dbPage = indexablePageRepository.findByPageId(
            IndexablePageId.of(dumpPage.getLang(), dumpPage.getId())
        );

        // Check if it is processable (by namespace)
        // Redirection pages are now considered processable but discarded when finding immutables
        try {
            pageValidator.validateProcessable(dumpPage);
        } catch (ReplacerException e) {
            // If the page is not processable then it should not exist in DB
            if (dbPage.isPresent()) {
                LOGGER.error("Unexpected page in DB not processable: {} - {}", dumpPage.getLang(), dumpPage.getTitle());
            }
            return DumpPageProcessorResult.PAGE_NOT_PROCESSABLE;
        }

        // Find the replacements to index and process the page
        try {
            return processPage(dumpPage, dbPage.orElse(null));
        } catch (Exception e) {
            // Just in case capture possible exceptions to continue processing other pages
            LOGGER.error("Page not processed: {}", dumpPage, e);
            return DumpPageProcessorResult.PAGE_NOT_PROCESSED;
        }
    }

    private DumpPageProcessorResult processPage(DumpPage dumpPage, @Nullable IndexablePage dbPage) {
        // Check if the last process of the page is after the dump generation, so we can skip it.
        Optional<LocalDate> dbLastUpdate = dbPage == null
            ? Optional.empty()
            : dbPage
                .getReplacements()
                .stream()
                .map(IndexableReplacement::getLastUpdate)
                .max(Comparator.comparing(LocalDate::toEpochDay));
        if (
            dbLastUpdate.isPresent() &&
            isNotProcessableByTimestamp(dumpPage, dbLastUpdate.get()) &&
            isNotProcessableByPageTitle(dumpPage, dbPage)
        ) {
            LOGGER.trace(
                "Page not processable by date: {}. Dump date: {}. DB date: {}",
                dumpPage.getTitle(),
                dumpPage.getLastUpdate(),
                dbLastUpdate
            );
            return DumpPageProcessorResult.PAGE_NOT_PROCESSED;
        }

        // Find the replacements in the dump page content
        List<Replacement> replacements = replacementFinderService.find(convertToFinder(dumpPage));

        // Index the found replacements against the ones in DB (if any)
        PageIndexResult pageIndexResult = pageIndexHelper.indexPageReplacements(
            convertToIndexable(dumpPage, replacements),
            dbPage
        );
        LOGGER.trace("Replacements to update: {}", pageIndexResult.size());

        if (pageIndexResult.isEmpty()) {
            return DumpPageProcessorResult.PAGE_NOT_PROCESSED;
        } else {
            pageIndexResultSaver.saveBatch(pageIndexResult);
            return DumpPageProcessorResult.PAGE_PROCESSED;
        }
    }

    private boolean isNotProcessableByTimestamp(DumpPage dumpPage, LocalDate dbDate) {
        // If page modified in dump equals to the last indexing, reprocess always.
        // If page modified in dump after last indexing, reprocess always.
        // If page modified in dump before last indexing, do not reprocess.
        return dumpPage.getLastUpdate().isBefore(dbDate);
    }

    private boolean isNotProcessableByPageTitle(DumpPage dumpPage, @Nullable IndexablePage dbPage) {
        // In case the page title has changed we force the page processing
        String dbPageTitle = dbPage == null ? null : dbPage.getTitle();
        return dumpPage.getTitle().equals(dbPageTitle);
    }

    private FinderPage convertToFinder(DumpPage page) {
        return FinderPage.of(page.getLang(), page.getContent(), page.getTitle());
    }

    private IndexablePage convertToIndexable(DumpPage dumpPage, List<Replacement> replacements) {
        return IndexablePage
            .builder()
            .id(IndexablePageId.of(dumpPage.getLang(), dumpPage.getId()))
            .title(dumpPage.getTitle())
            .lastUpdate(dumpPage.getLastUpdate())
            .replacements(replacements.stream().map(r -> convertToIndexable(r, dumpPage)).collect(Collectors.toList()))
            .build();
    }

    private IndexableReplacement convertToIndexable(Replacement replacement, DumpPage page) {
        return IndexableReplacement
            .builder()
            .indexablePageId(IndexablePageId.of(page.getLang(), page.getId()))
            .type(replacement.getType().getLabel())
            .subtype(replacement.getSubtype())
            .position(replacement.getStart())
            .context(replacement.getContext(page.getContent()))
            .lastUpdate(page.getLastUpdate())
            .build();
    }

    void finish(WikipediaLanguage lang) {
        pageIndexResultSaver.forceSave();
        indexablePageRepository.resetCache();
    }
}
