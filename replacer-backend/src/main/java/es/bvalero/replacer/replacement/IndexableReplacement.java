package es.bvalero.replacer.replacement;

import es.bvalero.replacer.common.WikipediaLanguage;
import java.time.LocalDate;
import lombok.Value;

@Value(staticConstructor = "of")
class IndexableReplacement {

    int pageId;
    WikipediaLanguage lang;
    String type;
    String subtype;
    int position;
    String context;
    LocalDate lastUpdate;
    String title;
}
