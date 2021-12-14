package es.bvalero.replacer.page.review;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Schema(description = "Page to review")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
public class ReviewPage {

    // Public: it is an in/out DTO

    private static final int SHORT_CONTENT_LENGTH = 50;

    @Schema(
        description = "Language of the Wikipedia in use",
        type = "string",
        allowableValues = { "es", "gl" },
        required = true
    )
    @NonNull
    @NotNull
    private String lang;

    @Schema(description = "Page ID", required = true, example = "6980716")
    @NonNull
    @NotNull
    private Integer id;

    @Schema(description = "Page title", required = true, example = "Artemio Zeno")
    @NonNull
    @NotNull
    private String title;

    @Schema(
        description = "Page (or section) content",
        required = true,
        example = "== Biografía ==Hijo de humildes inmigrantes piamonteses [...]"
    )
    @ToString.Exclude
    @NonNull
    @NotNull
    private String content;

    @Schema
    @Valid
    @Nullable
    private ReviewSection section;

    @Schema(
        description = "Timestamp when the page content was retrieved from Wikipedia",
        required = true,
        example = "2021-03-21T15:06:49Z"
    )
    @NonNull
    @NotNull
    private String queryTimestamp;

    @ToString.Include
    private String shortContent() {
        return StringUtils.abbreviate(this.getContent(), SHORT_CONTENT_LENGTH);
    }
}
