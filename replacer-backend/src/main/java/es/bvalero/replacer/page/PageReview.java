package es.bvalero.replacer.page;

import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

/**
 * Domain class of a page to review to be used in the front-end.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
class PageReview {

    private int id;
    private WikipediaLanguage lang;
    private String title;
    private String content;

    @Nullable
    private Integer section;

    private String anchor;
    private String queryTimestamp;
    private List<PageReplacement> replacements;
    private long numPending;
}
