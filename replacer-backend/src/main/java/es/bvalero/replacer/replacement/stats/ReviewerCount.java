package es.bvalero.replacer.replacement.stats;

import io.swagger.annotations.ApiModelProperty;
import lombok.Value;
import org.springframework.lang.NonNull;

@Value(staticConstructor = "of")
class ReviewerCount implements Comparable<ReviewerCount> {

    @ApiModelProperty(value = "Wikipedia user name", required = true, example = "Benjavalero")
    @NonNull
    String reviewer;

    @ApiModelProperty(required = true, example = "1")
    @NonNull
    Long count;

    @Override
    public int compareTo(ReviewerCount count) {
        return count.getCount().compareTo(this.count);
    }
}
