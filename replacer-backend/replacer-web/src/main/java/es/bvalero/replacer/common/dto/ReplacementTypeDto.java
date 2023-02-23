package es.bvalero.replacer.common.dto;

import es.bvalero.replacer.common.domain.ReplacementType;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.lang.NonNull;

@Schema(name = "ReplacementType")
@ParameterObject
@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
public class ReplacementTypeDto {

    @Schema(type = "integer", requiredMode = Schema.RequiredMode.REQUIRED)
    @Parameter(description = "Replacement kind code", required = true, example = "2")
    private byte kind;

    @Parameter(description = "Replacement subtype", required = true, example = "aún")
    @NonNull
    private String subtype;

    public static ReplacementTypeDto of(ReplacementType replacementType) {
        assert replacementType.isStandardType();
        return new ReplacementTypeDto(replacementType.getKind().getCode(), replacementType.getSubtype());
    }

    public ReplacementType toDomain() {
        return ReplacementType.ofType(this.kind, this.subtype);
    }
}
