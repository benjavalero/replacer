package es.bvalero.replacer.replacement;

import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.common.WikipediaNamespace;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Value;
import org.jetbrains.annotations.VisibleForTesting;

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
        return (
            "IndexablePage(id=" +
            this.getId() +
            ", lang=" +
            this.getLang().getCode() +
            ", title='" +
            this.getTitle() +
            "')"
        );
    }
}
