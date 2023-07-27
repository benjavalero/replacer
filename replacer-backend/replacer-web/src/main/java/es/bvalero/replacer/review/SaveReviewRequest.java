package es.bvalero.replacer.review;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import es.bvalero.replacer.common.util.ReplacerUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Collection;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Schema
@Data
public class SaveReviewRequest {

    @Schema(description = "Page reviewed", requiredMode = REQUIRED)
    @Valid
    @NotNull
    private ReviewPage page;

    @Schema(description = "Reviewed replacements", requiredMode = REQUIRED)
    @Valid
    @NotNull
    @NotEmpty
    private Collection<ReviewedReplacementDto> reviewedReplacements;

    @Override
    public String toString() {
        return ReplacerUtils.toJson(this);
    }
}
