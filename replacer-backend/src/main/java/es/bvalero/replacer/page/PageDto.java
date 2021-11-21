package es.bvalero.replacer.page;

import com.fasterxml.jackson.annotation.JsonInclude;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

@ApiModel(value = "Page", description = "Page to review")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class PageDto {

    private static final int CONTENT_SIZE = 50;

    @ApiModelProperty(value = "Language", allowableValues = "es, gl", required = true)
    private WikipediaLanguage lang;

    @ApiModelProperty(value = "Page ID", required = true, example = "6980716")
    private int id;

    @ApiModelProperty(value = "Page title", required = true, example = "Artemio Zeno")
    private String title;

    @ApiModelProperty(
        value = "Page (or section) content",
        required = true,
        example = "== Biografía ==Hijo de humildes inmigrantes piamonteses [...]"
    )
    private String content;

    @ApiModelProperty
    @Nullable
    private PageSection section;

    @ApiModelProperty(
        value = "Timestamp when the page content was retrieved from Wikipedia",
        required = true,
        example = "2021-03-21T15:06:49Z"
    )
    private String queryTimestamp;

    @Override
    public String toString() {
        return (
            "PageReview(lang=" +
            this.getLang() +
            ", id=" +
            this.getId() +
            ", title=" +
            this.getTitle() +
            ", content=" +
            StringUtils.abbreviate(this.getContent(), CONTENT_SIZE) +
            ", section=" +
            this.getSection() +
            ", queryTimestamp=" +
            this.getQueryTimestamp() +
            ")"
        );
    }
}
