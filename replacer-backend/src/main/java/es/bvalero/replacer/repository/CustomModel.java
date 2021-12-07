package es.bvalero.replacer.repository;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Value;
import org.springframework.lang.Nullable;

/**
 * Model entity representing a custom replacement in the database
 */
@Value
@Builder
public class CustomModel {

    @Nullable
    Long id; // Nullable when still to be created in database

    String lang;
    int pageId;
    String replacement;
    boolean cs;
    LocalDate lastUpdate;
    String reviewer;
}
