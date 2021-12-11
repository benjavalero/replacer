package es.bvalero.replacer.replacement.stats;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;
import org.springframework.lang.NonNull;

@Schema
@Value(staticConstructor = "of")
class ReviewerCount implements Comparable<ReviewerCount> {

    @Schema(description = "Name of the user in Wikipedia", required = true, example = "Benjavalero")
    @NonNull
    String reviewer;

    @Schema(required = true, example = "1")
    @NonNull
    Long count;

    @Override
    public int compareTo(ReviewerCount count) {
        return count.getCount().compareTo(this.count);
    }
}
