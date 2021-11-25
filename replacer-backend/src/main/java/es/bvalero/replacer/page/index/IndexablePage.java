package es.bvalero.replacer.page.index;

import es.bvalero.replacer.common.domain.WikipediaPageId;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
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

    @Nullable
    LocalDate lastUpdate;

    @NonNull
    List<IndexableReplacement> replacements;

    @Nullable
    public LocalDate getLastUpdate() {
        if (Objects.nonNull(this.lastUpdate)) {
            // The last-update date should exist when the page has been retrieved from Wikipedia or a dump
            return this.lastUpdate;
        } else {
            // The last-update won't exist (for the moment) if the page has been retrieved from database.
            // In this case we calculate the last-update with the latest date from the replacements.
            // In theory all pages have at least one replacement (the dummy one)
            // but just in case we consider that the response can be empty.
            return replacements
                .stream()
                .map(IndexableReplacement::getLastUpdate)
                .max(Comparator.comparing(LocalDate::toEpochDay))
                .orElse(null);
        }
    }
}
