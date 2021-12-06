package es.bvalero.replacer.replacement.stats;

import io.swagger.annotations.ApiModelProperty;
import lombok.NonNull;
import lombok.Value;

@Value(staticConstructor = "of")
class ReviewerCount {

    @ApiModelProperty(value = "Wikipedia user name", required = true, example = "Benjavalero")
    @NonNull
    String reviewer;

    @ApiModelProperty(required = true, example = "1")
    @NonNull
    Long count;
}
