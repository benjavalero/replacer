package es.bvalero.replacer.page.review;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import es.bvalero.replacer.common.util.ReplacerUtils;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.page.PageKey;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Collection;
import java.util.Objects;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.lang.Nullable;

@Schema(description = "Reviewed page. The page fields are only mandatory when saving the page with changes.")
@Data
public class ReviewedPage {

    @Schema(description = "Page title", requiredMode = NOT_REQUIRED, example = "Artemio Zeno")
    @Nullable
    private String title;

    @Schema(
        description = "Page (or section) content",
        requiredMode = NOT_REQUIRED,
        example = "== Biografía ==Hijo de humildes inmigrantes piamonteses [...]"
    )
    @JsonSerialize(using = PageContentSerializer.class)
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

    @JsonIgnore
    boolean isReviewedWithoutChanges() {
        return Objects.isNull(this.content);
    }

    void validate() {
        if (isReviewedWithoutChanges()) {
            if (
                this.content != null ||
                this.title != null ||
                this.sectionId != null ||
                this.sectionOffset != null ||
                this.queryTimestamp != null
            ) {
                throw new IllegalArgumentException("Unnecessary fields to save a reviewed page without changes");
            }
        } else {
            if (this.content == null || this.title == null || this.queryTimestamp == null) {
                throw new IllegalArgumentException("Missing mandatory fields to save a reviewed page with changes");
            }
        }
    }

    FinderPage toFinderPage(PageKey pageKey) {
        return new FinderPage() {
            @Override
            public PageKey getPageKey() {
                return pageKey;
            }

            @Override
            public String getTitle() {
                return Objects.requireNonNull(title);
            }

            @Override
            public String getContent() {
                return Objects.requireNonNull(content);
            }
        };
    }

    @Override
    public String toString() {
        return ReplacerUtils.toJson(this);
    }
}
