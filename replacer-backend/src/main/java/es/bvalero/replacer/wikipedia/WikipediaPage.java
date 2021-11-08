package es.bvalero.replacer.wikipedia;

import es.bvalero.replacer.domain.WikipediaLanguage;
import es.bvalero.replacer.page.validate.ValidatePage;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Value;
import lombok.With;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

/**
 * Domain object representing a page retrieved from Wikipedia.
 * It may also represent a specific section of a page,
 * though they are the same thing for Wikipedia API.
 */
@Value
@Builder
public class WikipediaPage implements ValidatePage {

    private static final int MAX_PRINTABLE_CONTENT_SIZE = 50;

    WikipediaLanguage lang;
    int id;
    WikipediaNamespace namespace;
    String title;
    String content;
    LocalDateTime lastUpdate; // Store time in case it is needed in the future
    LocalDateTime queryTimestamp; // Store the timestamp when the page was queried

    @With
    @Nullable
    WikipediaSection section; // Defined in case it is a section and null if it is the whole page

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
            WikipediaDateUtils.formatWikipediaTimestamp(this.getLastUpdate()) +
            ", queryTimestamp=" +
            WikipediaDateUtils.formatWikipediaTimestamp(this.getQueryTimestamp()) +
            "', section=" +
            this.getSection() +
            ")"
        );
    }
}
