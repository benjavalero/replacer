package es.bvalero.replacer.page.review;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotNull;
import lombok.Value;
import org.springframework.lang.NonNull;

@Schema(description = "Section of a page to review")
@Value(staticConstructor = "of")
class ReviewSection {

    @Schema(description = "Section ID", requiredMode = REQUIRED, example = "1")
    @NotNull
    int id;

    @Schema(description = "Section title", requiredMode = REQUIRED, example = "Biografía")
    @NonNull
    @NotNull
    String title;

    @Schema(description = "Offset of the section with the page content", requiredMode = REQUIRED, example = "1014")
    @NotNull
    int offset;
}
