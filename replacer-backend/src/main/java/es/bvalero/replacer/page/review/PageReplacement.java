package es.bvalero.replacer.page.review;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import lombok.Value;
import org.springframework.lang.NonNull;

@ApiModel(description = "Replacement to review")
@Value(staticConstructor = "of")
class PageReplacement {

    @ApiModelProperty(value = "Position of the replacement in the content", required = true, example = "1776")
    @NonNull
    Integer start;

    @ApiModelProperty(value = "Text of the replacement", example = "aún", required = true)
    @NonNull
    String text;

    @ApiModelProperty(value = "List of suggestions to fix the replacement", required = true)
    @NonNull
    List<PageReplacementSuggestion> suggestions;
}
