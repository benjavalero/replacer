package es.bvalero.replacer.wikipedia;

import es.bvalero.replacer.page.IndexablePage;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Value;
import lombok.With;
import org.springframework.lang.Nullable;

@Value
@Builder
public class WikipediaPage implements IndexablePage {

    // TODO: Make not public. Use interface IndexablePage outside this package when needed.

    int id;
    WikipediaLanguage lang;
    String title;
    WikipediaNamespace namespace;
    LocalDate lastUpdate;
    String content;

    @With(AccessLevel.PACKAGE)
    @Nullable
    Integer section;

    @With(AccessLevel.PACKAGE)
    @Nullable
    String anchor;

    // Store the timestamp when the page was queried. No need to convert it to Date format.
    String queryTimestamp;

    @Override
    public String toString() {
        return "WikipediaPage{" + "id=" + id + ", lang=" + lang.getCode() + ", title='" + title + '\'' + '}';
    }
}
