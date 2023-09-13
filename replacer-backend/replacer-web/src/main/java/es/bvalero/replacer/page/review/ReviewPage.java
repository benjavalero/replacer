package es.bvalero.replacer.page.review;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import es.bvalero.replacer.common.util.ReplacerUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Schema(description = "Page to review")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
class ReviewPage {

    @Schema(description = "Language of the Wikipedia in use", requiredMode = REQUIRED, example = "es")
    @NonNull
    @NotNull
    private String lang;

    @Schema(description = "Page ID", requiredMode = REQUIRED, example = "6980716")
    @NotNull
    private int pageId;

    @Schema(description = "Page title", requiredMode = REQUIRED, example = "Artemio Zeno")
    @NonNull
    @NotNull
    private String title;

    @Schema(
        description = "Page (or section) content. When saving without changes, it matches a string with an only whitespace.",
        requiredMode = REQUIRED,
        example = "== Biografía ==Hijo de humildes inmigrantes piamonteses [...]"
    )
    @JsonSerialize(using = PageContentSerializer.class)
    @NonNull
    @NotNull
    private String content;

    @Schema
    @Valid
    @Nullable
    private ReviewSection section;

    @Schema(
        description = "Timestamp when the page content was retrieved from Wikipedia",
        requiredMode = REQUIRED,
        example = "2021-03-21T15:06:49Z"
    )
    @NonNull
    @NotNull
    private String queryTimestamp;

    @Override
    public String toString() {
        return ReplacerUtils.toJson(this);
    }
}
