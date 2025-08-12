package es.bvalero.replacer.replacement.type;

import es.bvalero.replacer.common.util.ReplacerUtils;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.lang.NonNull;

@Schema(name = "ReplacementType")
@ParameterObject
@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(force = true)
public class ReplacementTypeDto {

    @Schema(type = "integer", requiredMode = Schema.RequiredMode.REQUIRED)
    @Parameter(description = "Replacement kind code", required = true, example = "2")
    private byte kind;

    @Parameter(description = "Replacement subtype", required = true, example = "aún")
    @NonNull
    private String subtype;

    public static ReplacementTypeDto of(byte kind, String subtype) {
        return new ReplacementTypeDto(kind, subtype);
    }

    @Override
    public String toString() {
        return ReplacerUtils.toJson(this);
    }
}
