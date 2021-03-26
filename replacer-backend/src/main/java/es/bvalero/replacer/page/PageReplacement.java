package es.bvalero.replacer.page;

import es.bvalero.replacer.finder.replacement.Suggestion;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import lombok.*;

@Value(staticConstructor = "of")
class PageReplacement {

    @ApiModelProperty(value = "Position of the replacement in the content", required = true, example = "1776")
    @With
    int start;

    @ApiModelProperty(value = "Text of the replacement", example = "aún", required = true)
    String text;

    @ApiModelProperty(value = "List of suggestions to fix the replacement", required = true)
    List<Suggestion> suggestions;

    int getEnd() {
        return this.start + this.text.length();
    }
}
