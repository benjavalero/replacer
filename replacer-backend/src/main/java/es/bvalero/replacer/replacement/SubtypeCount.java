package es.bvalero.replacer.replacement;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

@Value(staticConstructor = "of")
class SubtypeCount {

    @ApiModelProperty(value = "Replacement subtype", required = true, example = "habia")
    @JsonProperty("s")
    String subtype;

    @ApiModelProperty(value = "Number of pages containing this subtype to review", required = true, example = "1")
    @JsonProperty("c")
    @With
    long count;
}
