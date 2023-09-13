package es.bvalero.replacer.page.review;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import es.bvalero.replacer.common.util.ReplacerUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Collection;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;
import org.jetbrains.annotations.VisibleForTesting;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Schema(description = "Reviewed page")
@Data
public class ReviewedPage {

    @VisibleForTesting
    static final String EMPTY_CONTENT = " ";

    @Schema(
        description = "Page (or section) content. When saving without changes, it matches a string with an only whitespace.",
        requiredMode = REQUIRED,
        example = "== Biografía ==Hijo de humildes inmigrantes piamonteses [...]"
    )
    @JsonSerialize(using = PageContentSerializer.class)
    @NonNull
    @NotNull
    private String content;

    @Schema(description = "Section ID", requiredMode = NOT_REQUIRED, example = "1")
    @Nullable
    Integer sectionId;

    @Schema(description = "Offset of the section with the page content", requiredMode = NOT_REQUIRED, example = "1014")
    @Nullable
    Integer sectionOffset;

    @Schema(
        description = "Timestamp when the page content was retrieved from Wikipedia",
        requiredMode = REQUIRED,
        example = "2021-03-21T15:06:49Z"
    )
    @NonNull
    @NotNull
    private String queryTimestamp;

    @Schema(description = "Reviewed replacements", requiredMode = REQUIRED)
    @Valid
    @NotNull
    @NotEmpty
    private Collection<ReviewedReplacementDto> reviewedReplacements;

    @JsonIgnore
    boolean isReviewedWithoutChanges() {
        return this.content.equals(EMPTY_CONTENT);
    }

    @Override
    public String toString() {
        return ReplacerUtils.toJson(this);
    }
}
