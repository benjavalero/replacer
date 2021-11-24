package es.bvalero.replacer.page.index;

import es.bvalero.replacer.common.domain.WikipediaPageId;
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

    @NonNull
    WikipediaPageId id;

    // TODO: This should be non-null. To check and fix the cases in Production.
    @Nullable
    String title;

    // Not retrieved from database but from Wikipedia or a dump
    // TODO: For the moment it is needed in case a page has no replacements
    @Nullable
    LocalDate lastUpdate;

    @NonNull
    List<IndexableReplacement> replacements;
}
