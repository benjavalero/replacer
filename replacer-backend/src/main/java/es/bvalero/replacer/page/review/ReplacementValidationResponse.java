package es.bvalero.replacer.page.review;

import com.fasterxml.jackson.annotation.JsonInclude;
import es.bvalero.replacer.common.domain.ReplacementKind;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;
import org.springframework.lang.Nullable;

@Schema
@JsonInclude(JsonInclude.Include.NON_NULL)
@Value(staticConstructor = "of")
class ReplacementValidationResponse {

    @Schema(description = "Known replacement type or empty", example = "Ortografía")
    @Nullable
    ReplacementKind type;

    @Schema(description = "Known replacement subtype or empty", example = "aún")
    @Nullable
    String subtype;

    static ReplacementValidationResponse ofEmpty() {
        return ReplacementValidationResponse.of(null, null);
    }
}
