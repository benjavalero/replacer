package es.bvalero.replacer.repository;

import lombok.Builder;
import lombok.Value;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/** Model entity representing a custom replacement in the database */
@Value
@Builder
public class CustomModel {

    @Nullable
    Long id; // Nullable when still to be created in database

    @NonNull
    String lang;

    @NonNull
    Integer pageId;

    @NonNull
    String replacement;

    @NonNull
    Byte cs;

    @NonNull
    Integer position;

    @Nullable
    String reviewer;
}
