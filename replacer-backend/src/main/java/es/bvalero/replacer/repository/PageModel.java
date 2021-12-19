package es.bvalero.replacer.repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import lombok.Builder;
import lombok.Value;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/** Model entity representing a page in the database */
@Value
@Builder
public class PageModel {

    // TODO: There should exist a FK in DB to the pair (lang, pageId)
    @NonNull
    String lang;

    @NonNull
    Integer pageId;

    // TODO: This should be non-null. To check and fix the cases in Production.
    @Nullable
    String title;

    // TODO: This should be non-null once everything is re-indexed in database
    @Nullable
    LocalDate lastUpdate;

    @Builder.Default
    Collection<ReplacementModel> replacements = Collections.emptyList();
}
