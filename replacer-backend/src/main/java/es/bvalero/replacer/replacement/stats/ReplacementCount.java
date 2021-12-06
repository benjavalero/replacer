package es.bvalero.replacer.replacement.stats;

import io.swagger.annotations.ApiModelProperty;
import lombok.NonNull;
import lombok.Value;

@Value(staticConstructor = "of")
class ReplacementCount {

    @ApiModelProperty(required = true)
    @NonNull
    Long count;
}
