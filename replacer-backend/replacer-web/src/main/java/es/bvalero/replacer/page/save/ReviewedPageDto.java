package es.bvalero.replacer.page.save;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.annotation.JsonInclude;
import es.bvalero.replacer.common.util.ReplacerUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Collection;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

@Schema(
    description = "Reviewed page. The page fields are only mandatory when saving the page with changes.",
    name = "ReviewedPage"
)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
public class ReviewedPageDto {

    @Schema(description = "Page title", requiredMode = NOT_REQUIRED, example = "Artemio Zeno")
    @Nullable
    private String title;

    @Schema(
        description = "Page (or section) content",
        requiredMode = NOT_REQUIRED,
        example = "== Biografía ==Hijo de humildes inmigrantes piamonteses [...]"
    )
    @With(AccessLevel.PRIVATE)
    @Nullable
    private String content;

    @Schema(description = "Section ID", requiredMode = NOT_REQUIRED, example = "1")
    @Nullable
    Integer sectionId;

    @Schema(description = "Offset of the section with the page content", requiredMode = NOT_REQUIRED, example = "1014")
    @Nullable
    Integer sectionOffset;

    @Schema(
        description = "Timestamp when the page content was retrieved from Wikipedia",
        requiredMode = NOT_REQUIRED,
        example = "2021-03-21T15:06:49Z"
    )
    @Nullable
    private String queryTimestamp;

    @Schema(description = "Reviewed replacements", requiredMode = REQUIRED)
    @Valid
    @NotNull
    @NotEmpty
    private Collection<ReviewedReplacementDto> reviewedReplacements;

    @Override
    public String toString() {
        return ReplacerUtils.toJson(this.withContent(StringUtils.abbreviate(this.content, 50)));
    }
}
