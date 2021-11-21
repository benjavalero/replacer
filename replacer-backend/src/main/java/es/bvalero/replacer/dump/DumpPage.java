package es.bvalero.replacer.dump;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.domain.WikipediaNamespace;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Value;
import org.springframework.lang.NonNull;

/** Sub-domain object representing a Wikipedia page extracted from a dump XML file */
@Value
@Builder
class DumpPage {

    @NonNull
    WikipediaLanguage lang;

    @NonNull
    Integer id;

    @NonNull
    WikipediaNamespace namespace;

    @NonNull
    String title;

    @NonNull
    String content;

    @NonNull
    LocalDateTime lastUpdate;
}
