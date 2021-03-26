package es.bvalero.replacer.replacement;

import io.swagger.annotations.ApiModelProperty;
import lombok.Value;

@Value(staticConstructor = "of")
class ReplacementCount {

    @ApiModelProperty(required = true)
    Long count;
}
