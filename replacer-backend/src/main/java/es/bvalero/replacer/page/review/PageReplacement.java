package es.bvalero.replacer.page.review;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import lombok.Value;
import lombok.With;
import org.springframework.lang.NonNull;

@ApiModel(description = "Replacement to be reviewed")
@Value(staticConstructor = "of")
public class PageReplacement {

    // TODO: Public while refactoring

    @ApiModelProperty(value = "Position of the replacement in the content", required = true, example = "1776")
    @With // TODO: Check if needed after refactoring
    @NonNull
    Integer start;

    @ApiModelProperty(value = "Text of the replacement", example = "aún", required = true)
    @NonNull
    String text;

    @ApiModelProperty(value = "List of suggestions to fix the replacement", required = true)
    @NonNull
    List<PageReplacementSuggestion> suggestions;

    // TODO: Public while refactoring
    public int getEnd() {
        return this.start + this.text.length();
    }
}
