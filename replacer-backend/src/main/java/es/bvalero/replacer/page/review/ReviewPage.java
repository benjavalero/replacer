package es.bvalero.replacer.page.review;

import com.fasterxml.jackson.annotation.JsonInclude;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@ApiModel(value = "Page", description = "Page to review")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
public class ReviewPage {

    // Public: it is an in/out DTO

    private static final int CONTENT_SIZE = 50;

    @ApiModelProperty(value = "Language", allowableValues = "es, gl", required = true)
    @NonNull
    @NotNull
    private WikipediaLanguage lang;

    @ApiModelProperty(value = "Page ID", required = true, example = "6980716")
    @NonNull
    @NotNull
    private Integer id;

    @ApiModelProperty(value = "Page title", required = true, example = "Artemio Zeno")
    @NonNull
    @NotNull
    private String title;

    @ApiModelProperty(
        value = "Page (or section) content",
        required = true,
        example = "== Biografía ==Hijo de humildes inmigrantes piamonteses [...]"
    )
    @ToString.Exclude
    @NonNull
    @NotNull
    private String content;

    @ApiModelProperty
    @Valid
    @Nullable
    private ReviewSection section;

    @ApiModelProperty(
        value = "Timestamp when the page content was retrieved from Wikipedia",
        required = true,
        example = "2021-03-21T15:06:49Z"
    )
    @NonNull
    @NotNull
    private String queryTimestamp;

    @ToString.Include
    private String getAbbreviatedContent() {
        return StringUtils.abbreviate(this.getContent(), CONTENT_SIZE);
    }
}
