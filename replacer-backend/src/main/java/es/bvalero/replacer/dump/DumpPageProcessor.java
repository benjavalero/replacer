package es.bvalero.replacer.dump;

import com.jcabi.aspects.Loggable;
import es.bvalero.replacer.domain.ReplacerException;
import es.bvalero.replacer.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.replacement.Replacement;
import es.bvalero.replacer.finder.replacement.ReplacementFinderService;
import es.bvalero.replacer.page.index.IndexablePage;
import es.bvalero.replacer.page.index.IndexableReplacement;
import es.bvalero.replacer.page.index.PageIndexHelper;
import es.bvalero.replacer.page.repository.IndexablePageDB;
import es.bvalero.replacer.page.repository.IndexablePageRepository;
import es.bvalero.replacer.page.repository.IndexableReplacementDB;
import es.bvalero.replacer.page.validate.PageValidator;
import es.bvalero.replacer.replacement.ReplacementEntity;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
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
    private DumpWriter dumpWriter;

    @Autowired
    private ReplacementFinderService replacementFinderService;

    @Autowired
    private PageIndexHelper pageIndexHelper;

    @Value("${replacer.dump.batch.chunk.size}")
    private int chunkSize;

    private List<List<ReplacementEntity>> toWrite;

    @PostConstruct
    public void initializeToWrite() {
        this.toWrite = new ArrayList<>(chunkSize);
    }

    @Loggable(prepend = true, value = Loggable.TRACE)
    DumpPageProcessorResult process(DumpPage dumpPage) {
        // In all cases we find the current status of the page in the DB
        Optional<IndexablePageDB> dbPage = indexablePageRepository.findByPageId(dumpPage.getLang(), dumpPage.getId());

        // Check if it is processable (by namespace)
        // Redirection pages are now considered processable but discarded when finding immutables
        try {
            pageValidator.validateProcessable(dumpPage);
        } catch (ReplacerException e) {
            dbPage.ifPresent(this::notProcessPage);
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

    private void notProcessPage(IndexablePageDB dbPage) {
        // If the page is not processable then it should not exist in DB
        // There could be replacements reviewed by users that we want to keep for the sake of statistics
        // Therefore we remove all non-reviewed replacements of this page
        List<ReplacementEntity> toDelete = dbPage
            .convert()
            .stream()
            .filter(r -> r.isToBeReviewed() || r.isSystemReviewed())
            .map(ReplacementEntity::setToDelete)
            .collect(Collectors.toList());

        // The list can be empty
        addToWrite(toDelete);
    }

    private void addToWrite(List<ReplacementEntity> replacementEntities) {
        this.toWrite.add(replacementEntities);
        if (this.toWrite.size() >= chunkSize) {
            dumpWriter.write(this.toWrite);
            this.toWrite.clear();
        }
    }

    private DumpPageProcessorResult processPage(DumpPage dumpPage, @Nullable IndexablePageDB dbPage) {
        // Check if the last process of the page is after the dump generation, so we can skip it.
        Optional<LocalDate> dbLastUpdate = dbPage == null
            ? Optional.empty()
            : dbPage
                .getReplacements()
                .stream()
                .map(IndexableReplacementDB::getLastUpdate)
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
        List<ReplacementEntity> dbReplacements = dbPage == null ? Collections.emptyList() : dbPage.convert();
        List<ReplacementEntity> toUpdate = pageIndexHelper.findIndexPageReplacements(
            convertToIndexable(dumpPage),
            replacements.stream().map(r -> convert(r, dumpPage)).collect(Collectors.toList()),
            dbReplacements
        );
        LOGGER.trace("Replacements to update: {}", toUpdate.size());

        if (toUpdate.isEmpty()) {
            return DumpPageProcessorResult.PAGE_NOT_PROCESSED;
        } else {
            addToWrite(toUpdate);
            return DumpPageProcessorResult.PAGE_PROCESSED;
        }
    }

    private boolean isNotProcessableByTimestamp(DumpPage dumpPage, LocalDate dbDate) {
        // If page modified in dump equals to the last indexing, reprocess always.
        // If page modified in dump after last indexing, reprocess always.
        // If page modified in dump before last indexing, do not reprocess.
        return dumpPage.getLastUpdate().isBefore(dbDate);
    }

    private boolean isNotProcessableByPageTitle(DumpPage dumpPage, @Nullable IndexablePageDB dbPage) {
        // In case the page title has changed we force the page processing
        String dbPageTitle = dbPage == null ? null : dbPage.getTitle();
        return dumpPage.getTitle().equals(dbPageTitle);
    }

    private FinderPage convertToFinder(DumpPage page) {
        return FinderPage.of(page.getLang(), page.getContent(), page.getTitle());
    }

    private IndexablePage convertToIndexable(DumpPage dumpPage) {
        return IndexablePage
            .builder()
            .lang(dumpPage.getLang())
            .id(dumpPage.getId())
            .namespace(dumpPage.getNamespace())
            .title(dumpPage.getTitle())
            .content(dumpPage.getContent())
            .lastUpdate(dumpPage.getLastUpdate())
            .build();
    }

    private IndexableReplacement convert(Replacement replacement, DumpPage page) {
        return IndexableReplacement
            .builder()
            .lang(page.getLang())
            .pageId(page.getId())
            .type(replacement.getType().getLabel())
            .subtype(replacement.getSubtype())
            .position(replacement.getStart())
            .context(replacement.getContext(page.getContent()))
            .lastUpdate(page.getLastUpdate())
            .title(page.getTitle())
            .build();
    }

    void finish(WikipediaLanguage lang) {
        dumpWriter.write(toWrite);
        indexablePageRepository.resetCache(lang);
    }
}
