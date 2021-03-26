package es.bvalero.replacer.finder.replacement;

import com.fasterxml.jackson.annotation.JsonInclude;
import es.bvalero.replacer.finder.util.FinderUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Value;
import org.springframework.lang.Nullable;

@ApiModel(description = "Suggestion for a replacement")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Value(staticConstructor = "of")
public class Suggestion {

    @ApiModelProperty(value = "Fix proposed for a replacement", required = true, example = "aun")
    String text;

    @ApiModelProperty(value = "Description to explain the motivation of the fix", example = "incluso, aunque")
    @Nullable
    String comment;

    static Suggestion ofNoComment(String text) {
        return of(text, null);
    }

    Suggestion toUppercase() {
        return of(FinderUtils.setFirstUpperCase(text), comment);
    }
}
