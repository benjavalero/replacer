package es.bvalero.replacer.dump;

import com.jcabi.aspects.Loggable;
import es.bvalero.replacer.domain.ReplacerException;
import es.bvalero.replacer.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.replacement.Replacement;
import es.bvalero.replacer.finder.replacement.ReplacementFinderService;
import es.bvalero.replacer.page.index.IndexablePage;
import es.bvalero.replacer.page.index.IndexablePageValidator;
import es.bvalero.replacer.page.index.IndexableReplacement;
import es.bvalero.replacer.page.index.PageIndexHelper;
import es.bvalero.replacer.replacement.ReplacementEntity;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    private IndexablePageValidator indexablePageValidator;

    @Autowired
    private PageReplacementService pageReplacementService;

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
        // 1. Check if it is processable by namespace
        // We "skip" the item by throwing an exception
        try {
            indexablePageValidator.validateProcessable(convertToIndexable(dumpPage));
        } catch (ReplacerException e) {
            // If the page is not processable then it should not exist in DB ==> remove all replacements of this page
            // There could be replacements reviewed by users that we want to keep for the sake of statistics
            List<ReplacementEntity> toDelete = notProcessPage(dumpPage);
            if (!toDelete.isEmpty()) {
                addToWrite(toDelete);
            }
            return DumpPageProcessorResult.PAGE_NOT_PROCESSABLE;
        }

        // 2. Find the replacements to index
        try {
            List<ReplacementEntity> replacements = processPage(dumpPage);

            if (replacements.isEmpty()) {
                return DumpPageProcessorResult.PAGE_NOT_PROCESSED;
            } else {
                addToWrite(replacements);
                return DumpPageProcessorResult.PAGE_PROCESSED;
            }
        } catch (Exception e) {
            // Just in case capture possible exceptions to continue processing other pages
            LOGGER.error("Page not processed: {}", dumpPage, e);
            return DumpPageProcessorResult.PAGE_NOT_PROCESSED;
        }
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

    private List<ReplacementEntity> notProcessPage(DumpPage dumpPage) {
        List<ReplacementEntity> dbReplacements = pageReplacementService.findByPageId(
            dumpPage.getId(),
            dumpPage.getLang()
        );

        // Return the DB replacements not reviewed in order to delete them
        return dbReplacements
            .stream()
            .filter(r -> r.isToBeReviewed() || r.isSystemReviewed())
            .map(ReplacementEntity::setToDelete)
            .collect(Collectors.toList());
    }

    private void addToWrite(List<ReplacementEntity> replacementEntities) {
        this.toWrite.add(replacementEntities);
        if (this.toWrite.size() >= chunkSize) {
            dumpWriter.write(this.toWrite);
            this.toWrite.clear();
        }
    }

    private List<ReplacementEntity> processPage(DumpPage dumpPage) {
        List<ReplacementEntity> dbReplacements = pageReplacementService.findByPageId(
            dumpPage.getId(),
            dumpPage.getLang()
        );
        Optional<LocalDate> dbLastUpdate = dbReplacements
            .stream()
            .map(ReplacementEntity::getLastUpdate)
            .max(Comparator.comparing(LocalDate::toEpochDay));
        if (
            dbLastUpdate.isPresent() &&
            isNotProcessableByTimestamp(dumpPage, dbLastUpdate.get()) &&
            isNotProcessableByPageTitle(dumpPage, dbReplacements)
        ) {
            LOGGER.trace(
                "Page not processable by date: {}. Dump date: {}. DB date: {}",
                dumpPage.getTitle(),
                dumpPage.getLastUpdate(),
                dbLastUpdate
            );
            return Collections.emptyList();
        }

        List<Replacement> replacements = replacementFinderService.find(convertToFinder(dumpPage));
        List<ReplacementEntity> toUpdate = pageIndexHelper.findIndexPageReplacements(
            convertToIndexable(dumpPage),
            replacements.stream().map(r -> convert(r, dumpPage)).collect(Collectors.toList()),
            dbReplacements
        );
        LOGGER.trace("Replacements to update: {}", toUpdate.size());
        return toUpdate;
    }

    private boolean isNotProcessableByPageTitle(DumpPage dumpPage, List<ReplacementEntity> dbReplacements) {
        // In case the page title has changed we force the page processing
        return dbReplacements
            .stream()
            .filter(r -> !r.isDummy())
            .map(ReplacementEntity::getTitle)
            .allMatch(t -> dumpPage.getTitle().equals(t));
    }

    private boolean isNotProcessableByTimestamp(DumpPage dumpPage, LocalDate dbDate) {
        // If page modified in dump equals to the last indexing, reprocess always.
        // If page modified in dump after last indexing, reprocess always.
        // If page modified in dump before last indexing, do not reprocess.
        return dumpPage.getLastUpdate().isBefore(dbDate);
    }

    private FinderPage convertToFinder(DumpPage page) {
        return FinderPage.of(page.getLang(), page.getContent(), page.getTitle());
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
        pageReplacementService.finish(lang);
    }
}
