package es.bvalero.replacer.wikipedia;

import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.common.WikipediaNamespace;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Value;
import lombok.With;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

/** Domain object representing a page retrieved from Wikipedia */
@Value
@Builder
public class WikipediaPage {

    private static final int MAX_PRINTABLE_CONTENT_SIZE = 50;

    WikipediaLanguage lang;
    int id;
    WikipediaNamespace namespace;
    String title;
    String content;
    LocalDate lastUpdate;

    @With
    @Nullable
    WikipediaSection section;

    // Store the timestamp when the page was queried. No need to convert it to Date format.
    String queryTimestamp;

    @Override
    public String toString() {
        return (
            "WikipediaPage(lang=" +
            this.getLang() +
            ", id=" +
            this.getId() +
            ", namesapce=" +
            this.getNamespace() +
            ", title='" +
            this.getTitle() +
            ", content=" +
            StringUtils.abbreviate(this.getContent(), MAX_PRINTABLE_CONTENT_SIZE) +
            ", lastUpdate='" +
            this.getLastUpdate() +
            "', section=" +
            this.getSection() +
            ", queryTimestamp=" +
            this.getQueryTimestamp() +
            ")"
        );
    }
}
