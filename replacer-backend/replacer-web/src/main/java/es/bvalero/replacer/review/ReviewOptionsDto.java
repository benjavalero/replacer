package es.bvalero.replacer.review;

import es.bvalero.replacer.common.domain.ReplacementKind;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.lang.Nullable;

@ParameterObject
@Data
class ReviewOptionsDto {

    @Parameter(schema = @Schema(type = "integer"), description = "Replacement kind code", example = "2")
    @Nullable
    private Byte kind;

    @Parameter(description = "Replacement subtype", example = "aún")
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
        if (this.kind != null) {
            list.add(ReplacementKind.valueOf(this.kind).toString());
        }
        if (this.subtype != null) {
            list.add(this.subtype);
        }
        if (this.suggestion != null) {
            list.add(this.suggestion);
        }
        if (this.cs != null) {
            list.add(Boolean.toString(this.cs));
        }

        if (list.isEmpty()) {
            return "NO TYPE";
        } else {
            return StringUtils.join(list, " - ");
        }
    }
}
