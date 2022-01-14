package es.bvalero.replacer.repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import lombok.Builder;
import lombok.Value;
import org.springframework.lang.NonNull;

/** Model entity representing a page in the database */
@Value
@Builder
public class PageModel {

    @NonNull
    String lang;

    int pageId;

    @NonNull
    String title;

    @NonNull
    LocalDate lastUpdate;

    @Builder.Default
    Collection<ReplacementModel> replacements = new ArrayList<>();

    // Convenience method to add replacements while reading the results from database
    public void addReplacement(ReplacementModel replacement) {
        this.replacements.add(replacement);
    }
}
