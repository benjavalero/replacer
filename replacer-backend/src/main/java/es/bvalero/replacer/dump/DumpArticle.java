package es.bvalero.replacer.dump;

import es.bvalero.replacer.article.IndexableArticle;
import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Value
@Builder
class DumpArticle implements IndexableArticle {
    private int id;
    private String title;
    private WikipediaNamespace namespace;
    private LocalDate lastUpdate;
    private String content;

    boolean isProcessableByTimestamp(LocalDate dbDate) {
        // If article modified in dump equals to the last indexing, reprocess always.
        // If article modified in dump after last indexing, reprocess always.
        // If article modified in dump before last indexing, do not reprocess even when forcing.
        return !this.getLastUpdate().isBefore(dbDate);
    }

}
