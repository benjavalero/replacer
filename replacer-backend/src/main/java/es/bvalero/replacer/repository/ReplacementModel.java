package es.bvalero.replacer.repository;

import lombok.Builder;
import lombok.Value;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/** Model entity representing a replacement in the database */
@Value
@Builder
public class ReplacementModel {

    @Nullable
    Integer id; // Nullable when still to be created in database

    @NonNull
    String lang;

    int pageId;

    byte kind;

    @NonNull
    String subtype;

    int start;

    @NonNull
    String context;

    @Nullable
    String reviewer;
}
