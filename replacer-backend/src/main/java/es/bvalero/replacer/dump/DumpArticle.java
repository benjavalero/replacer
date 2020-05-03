package es.bvalero.replacer.dump;

import es.bvalero.replacer.article.IndexableArticle;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
class DumpArticle implements IndexableArticle {
    int id;
    WikipediaLanguage lang;
    String title;
    WikipediaNamespace namespace;
    LocalDate lastUpdate;
    String content;

    boolean isProcessableByTimestamp(LocalDate dbDate) {
        // If article modified in dump equals to the last indexing, reprocess always.
        // If article modified in dump after last indexing, reprocess always.
        // If article modified in dump before last indexing, do not reprocess even when forcing.
        return !this.getLastUpdate().isBefore(dbDate);
    }
}
