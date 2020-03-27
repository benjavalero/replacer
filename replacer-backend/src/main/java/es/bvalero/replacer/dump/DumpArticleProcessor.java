package es.bvalero.replacer.dump;

import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.ReplacementFindService;
import es.bvalero.replacer.replacement.ReplacementEntity;
import es.bvalero.replacer.replacement.ReplacementIndexService;
import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Process an article found in a Wikipedia dump.
 */
@Slf4j
@Component
public class DumpArticleProcessor implements ItemProcessor<DumpPage, List<ReplacementEntity>> {
    @Autowired
    private ReplacementCache replacementCache;

    @Autowired
    private ReplacementIndexService replacementIndexService;

    @Autowired
    private ReplacementFindService replacementFindService;

    @Override
    public List<ReplacementEntity> process(@NotNull DumpPage dumpPage) {
        // 1. Convert to indexable article
        DumpArticle dumpArticle = mapDumpPageToDumpArticle(dumpPage);

        // 2. Check if it is processable
        if (!dumpArticle.isProcessable()) {
            return null;
        }

        // 3. Find the replacements to index
        List<ReplacementEntity> replacements = processArticle(dumpArticle);
        return replacements.isEmpty() ? null : replacements;
    }

    private DumpArticle mapDumpPageToDumpArticle(DumpPage dumpPage) {
        return DumpArticle
            .builder()
            .id(dumpPage.id)
            .title(dumpPage.title)
            .namespace(WikipediaNamespace.valueOf(dumpPage.ns))
            .lastUpdate(WikipediaPage.parseWikipediaTimestamp(dumpPage.revision.timestamp))
            .content(dumpPage.revision.text)
            .build();
    }

    private List<ReplacementEntity> processArticle(DumpArticle dumpArticle) {
        LOGGER.debug("START Process dump article: {} - {}", dumpArticle.getId(), dumpArticle.getTitle());

        List<ReplacementEntity> dbReplacements = replacementCache.findByArticleId(dumpArticle.getId());
        Optional<LocalDate> dbLastUpdate = dbReplacements
            .stream()
            .map(ReplacementEntity::getLastUpdate)
            .max(Comparator.comparing(LocalDate::toEpochDay));
        if (dbLastUpdate.isPresent() && !dumpArticle.isProcessableByTimestamp(dbLastUpdate.get())) {
            LOGGER.debug(
                "END Process dump article. Not processable by date: {}. Dump date: {}. DB date: {}",
                dumpArticle.getTitle(),
                dumpArticle.getLastUpdate(),
                dbLastUpdate
            );
            return Collections.emptyList();
        }

        List<Replacement> replacements = replacementFindService.findReplacements(dumpArticle.getContent());
        List<ReplacementEntity> toWrite = replacementIndexService.findIndexArticleReplacements(
            dumpArticle.getId(),
            replacements.stream().map(dumpArticle::convertReplacementToIndexed).collect(Collectors.toList()),
            dbReplacements
        );

        LOGGER.debug("END Process dump article: {} - {}", dumpArticle.getId(), dumpArticle.getTitle());
        return toWrite;
    }
}
