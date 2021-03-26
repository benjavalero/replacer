package es.bvalero.replacer.wikipedia;

import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.common.WikipediaNamespace;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Value;
import lombok.With;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

@Value
@Builder
public class WikipediaPage {

    private static final int CONTENT_SIZE = 50;

    WikipediaLanguage lang;
    int id;
    String title;
    WikipediaNamespace namespace;
    LocalDate lastUpdate;
    String content;

    @With(AccessLevel.PACKAGE)
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
            ", title='" +
            this.getTitle() +
            ", lastUpdate='" +
            this.getLastUpdate() +
            ", content=" +
            StringUtils.abbreviate(this.getContent(), CONTENT_SIZE) +
            "', section=" +
            this.getSection() +
            ", queryTimestamp=" +
            this.getQueryTimestamp() +
            ")"
        );
    }
}
