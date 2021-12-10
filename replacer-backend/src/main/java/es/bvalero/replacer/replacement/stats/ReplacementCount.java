package es.bvalero.replacer.replacement.stats;

import io.swagger.annotations.ApiModelProperty;
import lombok.Value;
import org.springframework.lang.NonNull;

@Value(staticConstructor = "of")
class ReplacementCount {

    @ApiModelProperty(required = true)
    @NonNull
    Long count;
}
