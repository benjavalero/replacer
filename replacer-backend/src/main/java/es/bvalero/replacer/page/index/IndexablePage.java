package es.bvalero.replacer.page.index;

import java.time.LocalDate;
import java.util.Collection;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.springframework.lang.NonNull;

/** Page (to be) indexed in the database */
@NonFinal
@Value
@Builder
class IndexablePage {

    @NonNull
    IndexablePageId id;

    @NonNull
    String title;

    @NonNull
    LocalDate lastUpdate;

    @NonNull
    Collection<IndexableReplacement> replacements;
}
