package es.bvalero.replacer.dump;

import es.bvalero.replacer.page.IndexablePage;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
class DumpPage implements IndexablePage {

    int id;
    WikipediaLanguage lang;
    String title;
    WikipediaNamespace namespace;
    LocalDate lastUpdate;
    String content;

    boolean isProcessableByTimestamp(LocalDate dbDate) {
        // If page modified in dump equals to the last indexing, reprocess always.
        // If page modified in dump after last indexing, reprocess always.
        // If page modified in dump before last indexing, do not reprocess.
        return !this.getLastUpdate().isBefore(dbDate);
    }

    @Override
    public String toString() {
        return "DumpPage{" + "id=" + id + ", lang=" + lang.getCode() + ", title='" + title + '\'' + '}';
    }
}
