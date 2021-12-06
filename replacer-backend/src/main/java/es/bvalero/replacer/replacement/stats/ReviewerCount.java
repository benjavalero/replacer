package es.bvalero.replacer.replacement.stats;

import io.swagger.annotations.ApiModelProperty;
import lombok.Value;

@Value(staticConstructor = "of")
class ReviewerCount {

    @ApiModelProperty(value = "Wikipedia user name", required = true, example = "Benjavalero")
    String reviewer;

    @ApiModelProperty(required = true, example = "1")
    long count;
}
