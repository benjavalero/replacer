package es.bvalero.replacer.review;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;
import org.jspecify.annotations.NonNull;

@Schema(description = "Section of a page to review")
@Value(staticConstructor = "of")
class Section {

    @Schema(description = "Section ID", requiredMode = REQUIRED, example = "1")
    int id;

    @Schema(description = "Section title", requiredMode = REQUIRED, example = "Biografía")
    @NonNull
    String title;

    @Schema(description = "Offset of the section with the page content", requiredMode = REQUIRED, example = "1014")
    int offset;
}
