package es.bvalero.replacer.page.review;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageReviewSearch {

    // TODO: Public while refactoring

    // TODO: Move to PageReviewDto
    @ApiModelProperty(
        value = "Number of pending pages to review of the given type",
        required = true,
        example = "1704147"
    )
    private long numPending;

    @ApiModelProperty(value = "Replacement type", example = "Ortografía")
    @Nullable
    private String type;

    @ApiModelProperty(value = "Replacement subtype", example = "aún")
    @Nullable
    private String subtype;

    @ApiModelProperty(value = "Custom replacement suggestion", example = "todavía")
    @Nullable
    private String suggestion;

    @ApiModelProperty(value = "If the custom replacement is case-sensitive")
    @Nullable
    private Boolean cs;

    @Override
    public String toString() {
        List<String> list = new ArrayList<>();
        assert numPending >= 0;
        list.add(Long.toString(numPending));

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
