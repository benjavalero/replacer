package es.bvalero.replacer.page.review;

import com.fasterxml.jackson.annotation.JsonInclude;
import es.bvalero.replacer.common.util.ReplacerUtils;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.lang.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@ParameterObject
@Data
public class ReviewOptionsDto {

    @Parameter(schema = @Schema(type = "integer"), description = "Replacement kind code", example = "2")
    @Nullable
    private Byte kind;

    @Parameter(description = "Replacement subtype", example = "aún")
    @Nullable
    private String subtype;

    @Parameter(description = "If the custom replacement is case-sensitive")
    @Nullable
    private Boolean cs;

    @Parameter(description = "Custom replacement suggestion", example = "todavía")
    @Nullable
    private String suggestion;

    @Override
    public String toString() {
        return ReplacerUtils.toJson(this);
    }
}
