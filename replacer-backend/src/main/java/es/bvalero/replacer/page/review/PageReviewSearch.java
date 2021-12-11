package es.bvalero.replacer.page.review;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

@Schema(description = "Search options of the replacements to review")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
public class PageReviewSearch {

    // Public: it is an in/out DTO

    @Schema(description = "Replacement kind", example = "Ortografía")
    @Nullable
    private String type;

    @Schema(description = "Replacement subtype", example = "aún")
    @Size(max = 100)
    @Nullable
    private String subtype;

    @Schema(description = "Custom replacement suggestion", example = "todavía")
    @Nullable
    private String suggestion;

    @Schema(description = "If the custom replacement is case-sensitive")
    @Nullable
    private Boolean cs;

    @Override
    public String toString() {
        List<String> list = new ArrayList<>();

        if (type == null) {
            list.add("NO TYPE");
        } else {
            list.add(type);
            assert subtype != null;
            list.add(subtype);
            if (suggestion != null) {
                list.add(suggestion);
                assert cs != null;
                list.add(Boolean.toString(cs));
            }
        }

        return StringUtils.join(list, " - ");
    }
}
