package es.bvalero.replacer.page.index;

import java.time.LocalDate;
import java.util.Collection;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/** Page (to be) indexed in the database */
@NonFinal
@Value
@Builder
class IndexablePage {

    @NonNull
    IndexablePageId id;

    // TODO: This should be non-null. To check and fix the cases in Production.
    @Nullable
    String title;

    // TODO: This should be non-null once everything is re-indexed in database
    @Nullable
    LocalDate lastUpdate;

    @NonNull
    Collection<IndexableReplacement> replacements;
}
