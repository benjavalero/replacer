package es.bvalero.replacer.dump;

import es.bvalero.replacer.domain.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Value;
import org.springframework.lang.NonNull;

/** Sub-domain object representing a Wikipedia page extracted from a dump XML file */
@Value
@Builder
public class DumpPage {

    @NonNull
    WikipediaLanguage lang;

    @NonNull
    int id;

    @NonNull
    WikipediaNamespace namespace;

    @NonNull
    String title;

    @NonNull
    String content;

    @NonNull
    LocalDate lastUpdate;
}
