package es.bvalero.replacer.page.review;

import com.fasterxml.jackson.annotation.JsonInclude;
import es.bvalero.replacer.finder.replacement.ReplacementType;
import io.swagger.annotations.ApiModelProperty;
import lombok.Value;
import org.springframework.lang.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Value(staticConstructor = "of")
public class MisspellingType {

    // TODO: Public while refactoring

    @ApiModelProperty(value = "Known replacement type or empty", example = "Ortografía")
    @Nullable
    ReplacementType type;

    @ApiModelProperty(value = "Known replacement subtype or empty", example = "aún")
    @Nullable
    String subtype;

    // TODO: Public while refactoring
    public static MisspellingType ofEmpty() {
        return MisspellingType.of(null, null);
    }
}
