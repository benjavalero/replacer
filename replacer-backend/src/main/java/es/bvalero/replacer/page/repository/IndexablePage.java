package es.bvalero.replacer.page.repository;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Value;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/** Domain object representing a page (to be) indexed in the database */
@Value
@Builder
public class IndexablePage {

    // TODO: There should exist a FK in DB
    @NonNull
    IndexablePageId id;

    // TODO: This should be non-null. To check and fix the cases in Production.
    @Nullable
    String title;

    // Not retrieved from database but from Wikipedia or a dump
    // TODO: For the moment it is needed in case a page has no replacements
    @Nullable
    LocalDate lastUpdate;

    @NonNull
    List<IndexableReplacement> replacements;

    /* Named parameters to make easier the JDBC queries */

    public WikipediaLanguage getLang() {
        return this.id.getLang();
    }

    public Integer getPageId() {
        return this.id.getPageId();
    }
}
