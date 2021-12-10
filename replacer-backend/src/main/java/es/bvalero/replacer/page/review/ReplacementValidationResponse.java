package es.bvalero.replacer.page.review;

import com.fasterxml.jackson.annotation.JsonInclude;
import es.bvalero.replacer.common.domain.ReplacementType;
import io.swagger.annotations.ApiModelProperty;
import lombok.Value;
import org.springframework.lang.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Value(staticConstructor = "of")
class ReplacementValidationResponse {

    @ApiModelProperty(value = "Known replacement type or empty", example = "Ortografía")
    @Nullable
    ReplacementType type;

    @ApiModelProperty(value = "Known replacement subtype or empty", example = "aún")
    @Nullable
    String subtype;

    static ReplacementValidationResponse ofEmpty() {
        return ReplacementValidationResponse.of(null, null);
    }
}
