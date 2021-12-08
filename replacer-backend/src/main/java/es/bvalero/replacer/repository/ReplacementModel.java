package es.bvalero.replacer.repository;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Value;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/** Model entity representing a replacement in the database */
@Value
@Builder
public class ReplacementModel {

    @Nullable
    Long id; // Nullable when still to be created in database

    @NonNull
    String lang;

    @NonNull
    Integer pageId;

    @NonNull
    String type;

    @NonNull
    String subtype;

    @NonNull
    Integer position;

    @Nullable
    String context; // There are thousands of replacements reviewed before the context existed

    @NonNull
    LocalDate lastUpdate;

    @Nullable
    String reviewer;
}
