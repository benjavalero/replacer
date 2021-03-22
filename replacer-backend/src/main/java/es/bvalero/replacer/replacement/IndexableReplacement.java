package es.bvalero.replacer.replacement;

import es.bvalero.replacer.common.WikipediaLanguage;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class IndexableReplacement {

    WikipediaLanguage lang;
    int pageId;
    String type;
    String subtype;
    int position;
    String context;
    LocalDate lastUpdate;
    String title;
}
