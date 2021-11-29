package es.bvalero.replacer.page;

import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import lombok.Value;
import lombok.With;

@Value(staticConstructor = "of")
public class PageReplacement {

    // TODO: Public while refactoring

    @ApiModelProperty(value = "Position of the replacement in the content", required = true, example = "1776")
    @With
    int start;

    @ApiModelProperty(value = "Text of the replacement", example = "aún", required = true)
    String text;

    @ApiModelProperty(value = "List of suggestions to fix the replacement", required = true)
    List<PageReplacementSuggestion> suggestions;

    // TODO: Public while refactoring
    public int getEnd() {
        return this.start + this.text.length();
    }
}
