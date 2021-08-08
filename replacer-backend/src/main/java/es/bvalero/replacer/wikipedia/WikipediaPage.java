package es.bvalero.replacer.wikipedia;

import es.bvalero.replacer.common.DateUtils;
import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.common.WikipediaNamespace;
import java.time.LocalDateTime;
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
    LocalDateTime lastUpdate; // Store time in case it is needed in the future

    @With
    @Nullable
    WikipediaSection section;

    LocalDateTime queryTimestamp; // Store the timestamp when the page was queried

    @Override
    public String toString() {
        return (
            "WikipediaPage(lang=" +
            this.getLang().getCode() +
            ", id=" +
            this.getId() +
            ", namesapce=" +
            this.getNamespace() +
            ", title='" +
            this.getTitle() +
            ", content=" +
            StringUtils.abbreviate(this.getContent(), MAX_PRINTABLE_CONTENT_SIZE) +
            ", lastUpdate='" +
            DateUtils.formatWikipediaTimestamp(this.getLastUpdate()) +
            "', section=" +
            this.getSection() +
            ", queryTimestamp=" +
            DateUtils.formatWikipediaTimestamp(this.getQueryTimestamp()) +
            ")"
        );
    }
}
