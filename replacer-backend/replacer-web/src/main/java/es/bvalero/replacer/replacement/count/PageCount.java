package es.bvalero.replacer.replacement.count;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;
import org.springframework.lang.NonNull;

@Schema(description = "Count of reviewed replacements by page")
@Value(staticConstructor = "of")
class PageCount {

    @Schema(requiredMode = REQUIRED, example = "1")
    int pageId;

    @Schema(description = "Page title", requiredMode = REQUIRED, example = "Andorra")
    @NonNull
    String title;

    @Schema(requiredMode = REQUIRED, example = "1")
    int count;
}
