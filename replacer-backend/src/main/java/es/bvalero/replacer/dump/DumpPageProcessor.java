package es.bvalero.replacer.dump;

import com.jcabi.aspects.Loggable;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.page.index.IndexablePage;
import es.bvalero.replacer.page.index.IndexablePageMapper;
import es.bvalero.replacer.page.index.PageIndexer;
import es.bvalero.replacer.page.repository.PageRepository;
import es.bvalero.replacer.page.validate.PageValidator;
import java.time.LocalDate;
import java.util.Optional;
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
    @Qualifier("pageCacheRepository")
    private PageRepository pageRepository;

    @Autowired
    private PageIndexer pageIndexer;

    @Loggable(prepend = true, value = Loggable.TRACE)
    DumpPageProcessorResult process(DumpPage dumpPage) {
        WikipediaPage page = DumpPageMapper.toDomain(dumpPage);

        // In all cases we find the current status of the page in the DB
        Optional<IndexablePage> dbPage = Optional.ofNullable(
            IndexablePageMapper.fromModel(pageRepository.findByPageId(page.getId()).orElse(null))
        );

        // Check if it is processable (by namespace)
        // Redirection pages are now considered processable but discarded when finding immutables
        try {
            pageValidator.validateProcessable(page);
        } catch (ReplacerException e) {
            // If the page is not processable then it should not exist in DB
            dbPage.ifPresent(
                dbIndexablePage -> {
                    LOGGER.error(
                        "Unexpected page in DB not processable: {} - {}",
                        page.getId().getLang(),
                        page.getTitle()
                    );
                    pageIndexer.indexObsoletePage(dbIndexablePage, true);
                }
            );
            return DumpPageProcessorResult.PAGE_NOT_PROCESSABLE;
        }

        // Find the replacements to index and process the page
        try {
            return processPage(page, dbPage.orElse(null));
        } catch (Exception e) {
            // Just in case capture possible exceptions to continue processing other pages
            LOGGER.error("Page not processed: {}", dumpPage, e);
            return DumpPageProcessorResult.PAGE_NOT_PROCESSED;
        }
    }

    private DumpPageProcessorResult processPage(WikipediaPage page, @Nullable IndexablePage dbPage) {
        // Check if the last process of the page is after the dump generation, so we can skip it.
        Optional<LocalDate> dbLastUpdate = dbPage == null
            ? Optional.empty()
            : Optional.ofNullable(dbPage.getLastUpdate());
        if (
            dbLastUpdate.isPresent() &&
            isNotProcessableByTimestamp(page, dbLastUpdate.get()) &&
            isNotProcessableByPageTitle(page, dbPage)
        ) {
            LOGGER.trace(
                "Page not processable by date: {}. Dump date: {}. DB date: {}",
                page.getTitle(),
                page.getLastUpdate(),
                dbLastUpdate
            );
            return DumpPageProcessorResult.PAGE_NOT_PROCESSED;
        }

        // Index the found replacements against the ones in DB (if any)
        boolean pageProcessed = pageIndexer.indexPageReplacements(page, dbPage);

        return pageProcessed ? DumpPageProcessorResult.PAGE_PROCESSED : DumpPageProcessorResult.PAGE_NOT_PROCESSED;
    }

    private boolean isNotProcessableByTimestamp(WikipediaPage page, LocalDate dbDate) {
        // If page modified in dump equals to the last indexing, reprocess always.
        // If page modified in dump after last indexing, reprocess always.
        // If page modified in dump before last indexing, do not reprocess.
        return page.getLastUpdate().toLocalDate().isBefore(dbDate);
    }

    private boolean isNotProcessableByPageTitle(WikipediaPage page, @Nullable IndexablePage dbPage) {
        // In case the page title has changed we force the page processing
        String dbPageTitle = dbPage == null ? null : dbPage.getTitle();
        return page.getTitle().equals(dbPageTitle);
    }

    void finish() {
        pageIndexer.forceSave();
        pageRepository.resetCache();
    }
}
