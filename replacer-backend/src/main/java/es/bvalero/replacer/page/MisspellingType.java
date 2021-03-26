package es.bvalero.replacer.page;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import lombok.Value;
import org.springframework.lang.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Value(staticConstructor = "of")
class MisspellingType {

    @ApiModelProperty(value = "Known replacement type or empty", example = "Ortografía")
    @Nullable
    String type;

    @ApiModelProperty(value = "Known replacement subtype or empty", example = "aún")
    @Nullable
    String subtype;

    static MisspellingType ofEmpty() {
        return MisspellingType.of(null, null);
    }
}
