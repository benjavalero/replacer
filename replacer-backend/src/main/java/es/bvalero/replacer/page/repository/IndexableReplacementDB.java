package es.bvalero.replacer.page.repository;

import java.time.LocalDate;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.springframework.lang.Nullable;

// TODO: This is a temporary class until it is unified with the existing IndexableReplacement and maybe ReplacementEntity

@Value
@Builder
public class IndexableReplacementDB {

    @Nullable
    Long id; // Nullable when still to be created in database

    @NonNull
    String type;

    @NonNull
    String subtype;

    @NonNull
    Integer position;

    @NonNull
    String context;

    @NonNull
    LocalDate lastUpdate;

    @Nullable
    String reviewer;
}
