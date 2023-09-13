package es.bvalero.replacer.page.review;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import es.bvalero.replacer.common.util.ReplacerUtils;
import es.bvalero.replacer.page.PageContentSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Collection;
import lombok.Builder;
import lombok.Value;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Schema(description = "Page to review")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Value
@Builder
public class ReviewPage {

    @Schema(description = "Language of the Wikipedia in use", requiredMode = REQUIRED, example = "es")
    @NonNull
    String lang;

    @Schema(description = "Page ID", requiredMode = REQUIRED, example = "6980716")
    int pageId;

    @Schema(description = "Page title", requiredMode = REQUIRED, example = "Artemio Zeno")
    @NonNull
    String title;

    @Schema(
        description = "Page (or section) content. When saving without changes, it matches a string with an only whitespace.",
        requiredMode = REQUIRED,
        example = "== Biografía ==Hijo de humildes inmigrantes piamonteses [...]"
    )
    @JsonSerialize(using = PageContentSerializer.class)
    @NonNull
    String content;

    @Schema
    @Nullable
    ReviewSection section;

    @Schema(
        description = "Timestamp when the page content was retrieved from Wikipedia",
        requiredMode = REQUIRED,
        example = "2021-03-21T15:06:49Z"
    )
    @NonNull
    String queryTimestamp;

    @Schema(description = "Collection of replacements to review", requiredMode = REQUIRED)
    @NonNull
    Collection<ReviewReplacement> replacements;

    @Override
    public String toString() {
        return ReplacerUtils.toJson(this);
    }
}
