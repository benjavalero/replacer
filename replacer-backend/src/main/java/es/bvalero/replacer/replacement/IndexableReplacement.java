package es.bvalero.replacer.replacement;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.LocalDate;

@Value(staticConstructor = "of")
public class IndexableReplacement {
    private int articleId;
    private String type;
    private String subtype;
    private int position;
    private LocalDate lastUpdate;
}
