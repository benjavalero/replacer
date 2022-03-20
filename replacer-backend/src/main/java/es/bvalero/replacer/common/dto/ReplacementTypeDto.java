package es.bvalero.replacer.common.dto;

import es.bvalero.replacer.common.domain.ReplacementType;
import io.swagger.v3.oas.annotations.Parameter;
import javax.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.lang.NonNull;

@ParameterObject
@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
public class ReplacementTypeDto {

    @Parameter(description = "Replacement kind code", required = true, example = "2")
    private byte kind;

    @Parameter(description = "Replacement subtype", required = true, example = "aún")
    @NonNull
    @NotBlank
    private String subtype;

    public static ReplacementTypeDto fromDomain(ReplacementType replacementType) {
        return new ReplacementTypeDto(replacementType.getKind().getCode(), replacementType.getSubtype());
    }

    public ReplacementType toDomain() {
        return ReplacementType.of(this.getKind(), this.getSubtype());
    }
}
