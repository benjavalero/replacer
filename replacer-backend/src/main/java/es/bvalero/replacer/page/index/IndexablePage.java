package es.bvalero.replacer.page.index;

import es.bvalero.replacer.domain.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class IndexablePage {

    WikipediaLanguage lang;
    int id;
    WikipediaNamespace namespace;
    String title;
    String content;
    LocalDate lastUpdate;

    @Override
    public String toString() {
        return ("IndexablePage(id=" + this.getId() + ", lang=" + this.getLang() + ", title='" + this.getTitle() + "')");
    }
}
