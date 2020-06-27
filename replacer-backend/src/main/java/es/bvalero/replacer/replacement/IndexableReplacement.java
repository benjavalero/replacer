package es.bvalero.replacer.replacement;

import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import java.time.LocalDate;
import lombok.Value;

@Value(staticConstructor = "of")
public class IndexableReplacement {
    int pageId;
    WikipediaLanguage lang;
    String type;
    String subtype;
    int position;
    String context;
    LocalDate lastUpdate;
}
