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
 * Process a page found in a Wikipedia dump.
 */
@Slf4j
@StepScope
@Component
public class DumpPageProcessor implements ItemProcessor<DumpPageXml, List<ReplacementEntity>> {
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
        // 1. Convert to indexable page
        DumpPage dumpPage = mapDumpPageXmlToDumpPage(dumpPageXml);

        // 2. Check if it is processable
        if (!dumpPage.isProcessable(ignorableTemplates)) {
            return null;
        }

        // 3. Find the replacements to index
        List<ReplacementEntity> replacements = processArticle(dumpPage);
        return replacements.isEmpty() ? null : replacements;
    }

    private DumpPage mapDumpPageXmlToDumpPage(DumpPageXml dumpPageXml) {
        return DumpPage
            .builder()
            .id(dumpPageXml.id)
            .lang(WikipediaLanguage.forValues(dumpLang))
            .title(dumpPageXml.title)
            .namespace(WikipediaNamespace.valueOf(dumpPageXml.ns))
            .lastUpdate(WikipediaPage.parseWikipediaTimestamp(dumpPageXml.revision.timestamp))
            .content(dumpPageXml.revision.text)
            .build();
    }

    List<ReplacementEntity> processArticle(DumpPage dumpPage) {
        LOGGER.debug("START Process dump page: {} - {}", dumpPage.getId(), dumpPage.getTitle());

        List<ReplacementEntity> dbReplacements = replacementCache.findByArticleId(dumpPage.getId(), dumpPage.getLang());
        Optional<LocalDate> dbLastUpdate = dbReplacements
            .stream()
            .map(ReplacementEntity::getLastUpdate)
            .max(Comparator.comparing(LocalDate::toEpochDay));
        if (dbLastUpdate.isPresent() && !dumpPage.isProcessableByTimestamp(dbLastUpdate.get())) {
            LOGGER.debug(
                "END Process dump page. Not processable by date: {}. Dump date: {}. DB date: {}",
                dumpPage.getTitle(),
                dumpPage.getLastUpdate(),
                dbLastUpdate
            );
            return Collections.emptyList();
        }

        List<Replacement> replacements = replacementFindService.findReplacements(
            dumpPage.getContent(),
            dumpPage.getLang()
        );
        List<ReplacementEntity> toWrite = replacementIndexService.findIndexPageReplacements(
            dumpPage.getId(),
            dumpPage.getLang(),
            replacements.stream().map(dumpPage::convertReplacementToIndexed).collect(Collectors.toList()),
            dbReplacements
        );

        LOGGER.debug("END Process dump page: {} - {}", dumpPage.getId(), dumpPage.getTitle());
        return toWrite;
    }
}
