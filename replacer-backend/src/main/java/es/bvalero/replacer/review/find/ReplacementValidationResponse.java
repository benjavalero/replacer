package es.bvalero.replacer.review.find;

import com.fasterxml.jackson.annotation.JsonInclude;
import es.bvalero.replacer.common.domain.ReplacementType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;
import org.springframework.lang.NonNull;

@Schema
@JsonInclude(JsonInclude.Include.NON_NULL)
@Value(staticConstructor = "of")
class ReplacementValidationResponse {

    @Schema(description = "Known replacement kind or empty", example = "Ortografía")
    byte kind;

    @Schema(description = "Known replacement subtype or empty", example = "aún")
    @NonNull
    String subtype;

    static ReplacementValidationResponse of(ReplacementType replacementType) {
        return ReplacementValidationResponse.of(replacementType.getKind().getCode(), replacementType.getSubtype());
    }
}