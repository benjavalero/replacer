package es.bvalero.replacer.repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import lombok.Builder;
import lombok.Value;
import org.springframework.lang.NonNull;

/** Model entity representing a page in the database */
@Value
@Builder
public class PageModel {

    @NonNull
    String lang;

    @NonNull
    Integer pageId;

    @NonNull
    String title;

    @NonNull
    LocalDate lastUpdate;

    @Builder.Default
    Collection<ReplacementModel> replacements = Collections.emptyList();
}
