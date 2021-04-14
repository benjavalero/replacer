package es.bvalero.replacer.replacement;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Value;
import org.springframework.lang.Nullable;

/**
 * A custom replacement in the database related to a page.
 */
@Value
@Builder
public class CustomEntity {

    @Nullable
    Long id; // Nullable when still to be created in database

    String lang;
    int pageId;
    String replacement;
    boolean cs;
    LocalDate lastUpdate;
    String reviewer;
}
