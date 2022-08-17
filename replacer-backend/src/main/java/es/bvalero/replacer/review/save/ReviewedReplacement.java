package es.bvalero.replacer.review.save;

import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.WikipediaPageId;
import lombok.Builder;
import lombok.Value;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Value
@Builder
public class ReviewedReplacement {

    @NonNull
    WikipediaPageId pageId;

    @NonNull
    ReplacementType type;

    @Nullable
    Boolean cs; // Only for custom replacements

    int start;

    @NonNull
    String reviewer;

    boolean fixed;
}
