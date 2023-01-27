package es.bvalero.replacer.review;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import es.bvalero.replacer.common.dto.Language;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Schema(name = "ReviewPage")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
class ReviewPageDto {

    private static final int SHORT_CONTENT_LENGTH = 50;

    @Schema(description = "Language of the Wikipedia in use", requiredMode = REQUIRED)
    @NonNull
    @NotNull
    private Language lang;

    @Schema(description = "Page ID", requiredMode = REQUIRED, example = "6980716")
    @NotNull
    private int pageId;

    @Schema(description = "Page title", requiredMode = REQUIRED, example = "Artemio Zeno")
    @NonNull
    @NotNull
    private String title;

    @Schema(
        description = "Page (or section) content",
        requiredMode = REQUIRED,
        example = "== Biografía ==Hijo de humildes inmigrantes piamonteses [...]"
    )
    @ToString.Exclude
    @NonNull
    @NotNull
    private String content;

    @Schema
    @Valid
    @Nullable
    private ReviewSectionDto section;

    @Schema(
        description = "Timestamp when the page content was retrieved from Wikipedia",
        requiredMode = REQUIRED,
        example = "2021-03-21T15:06:49Z"
    )
    @NonNull
    @NotNull
    private String queryTimestamp;

    @ToString.Include
    private String shortContent() {
        return StringUtils.abbreviate(getContent(), SHORT_CONTENT_LENGTH);
    }

    @JsonIgnore
    public int getSectionOffset() {
        return this.section != null ? this.section.getOffset() : 0;
    }
}
