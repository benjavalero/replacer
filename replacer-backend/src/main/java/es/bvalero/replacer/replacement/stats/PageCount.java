package es.bvalero.replacer.replacement.stats;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;
import org.springframework.lang.NonNull;

@Schema
@Value(staticConstructor = "of")
class PageCount {

    @Schema(description = "Page ID", required = true, example = "1")
    int pageId;

    @Schema(description = "Page title", required = true, example = "Andorra")
    @NonNull
    String title;

    @Schema(required = true, example = "1")
    int count;
}
