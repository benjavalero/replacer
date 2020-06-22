package es.bvalero.replacer.dump;

import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.ReplacementFindService;
import es.bvalero.replacer.replacement.ReplacementEntity;
import es.bvalero.replacer.replacement.ReplacementIndexService;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Process an article found in a Wikipedia dump.
 */
@Slf4j
@StepScope
@Component
public class DumpArticleProcessor implements ItemProcessor<DumpPageXml, List<ReplacementEntity>> {
    @Autowired
    private ReplacementCache replacementCache;

    @Autowired
    private ReplacementIndexService replacementIndexService;

    @Autowired
    private ReplacementFindService replacementFindService;

    @Value("#{jobParameters[dumpLang]}")
    private String dumpLang;

    @Resource
    private List<String> ignorableTemplates;

    @Override
    public List<ReplacementEntity> process(@NotNull DumpPageXml dumpPageXml) {
        // 1. Convert to indexable article
        DumpArticle dumpArticle = mapDumpPageToDumpArticle(dumpPageXml);

        // 2. Check if it is processable
        if (!dumpArticle.isProcessable(ignorableTemplates)) {
            return null;
        }

        // 3. Find the replacements to index
        List<ReplacementEntity> replacements = processArticle(dumpArticle);
        return replacements.isEmpty() ? null : replacements;
    }

    private DumpArticle mapDumpPageToDumpArticle(DumpPageXml dumpPageXml) {
        return DumpArticle
            .builder()
            .id(dumpPageXml.id)
            .lang(WikipediaLanguage.forValues(dumpLang))
            .title(dumpPageXml.title)
            .namespace(WikipediaNamespace.valueOf(dumpPageXml.ns))
            .lastUpdate(WikipediaPage.parseWikipediaTimestamp(dumpPageXml.revision.timestamp))
            .content(dumpPageXml.revision.text)
            .build();
    }

    List<ReplacementEntity> processArticle(DumpArticle dumpArticle) {
        LOGGER.debug("START Process dump article: {} - {}", dumpArticle.getId(), dumpArticle.getTitle());

        List<ReplacementEntity> dbReplacements = replacementCache.findByArticleId(
            dumpArticle.getId(),
            dumpArticle.getLang()
        );
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

        List<Replacement> replacements = replacementFindService.findReplacements(
            dumpArticle.getContent(),
            dumpArticle.getLang()
        );
        List<ReplacementEntity> toWrite = replacementIndexService.findIndexArticleReplacements(
            dumpArticle.getId(),
            dumpArticle.getLang(),
            replacements.stream().map(dumpArticle::convertReplacementToIndexed).collect(Collectors.toList()),
            dbReplacements
        );

        LOGGER.debug("END Process dump article: {} - {}", dumpArticle.getId(), dumpArticle.getTitle());
        return toWrite;
    }
}
