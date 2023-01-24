package es.bvalero.replacer.review;

import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.common.domain.ReplacementType;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.lang.Nullable;

@ParameterObject
@Data
@NoArgsConstructor
class ReviewOptionsDto {

    @Parameter(schema = @Schema(type = "integer"), description = "Replacement kind code", example = "2")
    @Nullable
    private Byte kind;

    @Parameter(description = "Replacement subtype", example = "aún")
    @Size(max = ReplacementType.MAX_SUBTYPE_LENGTH)
    @Nullable
    private String subtype;

    @Parameter(description = "Custom replacement suggestion", example = "todavía")
    @Nullable
    private String suggestion;

    @Parameter(description = "If the custom replacement is case-sensitive")
    @Nullable
    private Boolean cs;

    @Override
    public String toString() {
        List<String> list = new ArrayList<>();

        if (this.kind == null) {
            list.add("NO TYPE");
        } else {
            list.add(ReplacementKind.valueOf(this.kind).toString());
            assert this.subtype != null;
            list.add(this.subtype);
            if (this.suggestion != null) {
                list.add(this.suggestion);
                assert this.cs != null;
                list.add(Boolean.toString(this.cs));
            }
        }

        return StringUtils.join(list, " - ");
    }
}
