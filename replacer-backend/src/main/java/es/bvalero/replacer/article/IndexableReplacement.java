package es.bvalero.replacer.article;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
class IndexableReplacement {
    private int articleId;
    private String type;
    private String subtype;
    private int position;
    private LocalDate lastUpdate;
}
