package es.bvalero.replacer.page.repository;

import es.bvalero.replacer.domain.WikipediaLanguage;
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
    IndexablePageId indexablePageId;

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

    /* Named parameters to make easier the JDBC queries */

    WikipediaLanguage getLang() {
        return this.indexablePageId.getLang();
    }

    Integer getPageId() {
        return this.indexablePageId.getPageId();
    }
}
