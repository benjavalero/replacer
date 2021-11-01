package es.bvalero.replacer.page;

import com.fasterxml.jackson.annotation.JsonIgnore;
import es.bvalero.replacer.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.replacement.ReplacementType;
import io.swagger.annotations.ApiParam;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.TestOnly;
import org.springframework.lang.Nullable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class PageReviewOptions {

    @ApiParam(value = "Language", required = true)
    private WikipediaLanguage lang;

    @ApiParam(value = "Wikipedia user name", required = true, example = "Benjavalero")
    private String user;

    @ApiParam(value = "Replacement type", example = "Ortografía")
    @Nullable
    private String type; // TODO: ReplacementType

    @Size(max = 100)
    @ApiParam(value = "Replacement subtype", example = "aún")
    @Nullable
    private String subtype;

    @ApiParam(value = "Custom replacement suggestion", example = "todavía")
    @Nullable
    private String suggestion;

    @ApiParam(value = "If the custom replacement is case-sensitive")
    @Nullable
    private Boolean cs;

    @JsonIgnore
    @Nullable
    Integer offset = null;

    void incrementOffset(int increment) {
        this.offset = this.offset == null ? 0 : this.offset + increment;
    }

    @TestOnly
    static PageReviewOptions ofNoType() {
        return PageReviewOptions.builder().lang(WikipediaLanguage.getDefault()).build();
    }

    @TestOnly
    static PageReviewOptions ofTypeSubtype(String type, String subtype) {
        return PageReviewOptions.builder().lang(WikipediaLanguage.getDefault()).type(type).subtype(subtype).build();
    }

    @TestOnly
    static PageReviewOptions ofCustom(
        WikipediaLanguage lang,
        String replacement,
        String suggestion,
        boolean caseSensitive
    ) {
        return PageReviewOptions
            .builder()
            .lang(lang)
            .type(ReplacementType.CUSTOM.getLabel())
            .subtype(replacement)
            .suggestion(suggestion)
            .cs(caseSensitive)
            .build();
    }

    @Override
    public String toString() {
        List<String> list = new ArrayList<>();
        list.add(user);
        list.add(lang.toString());

        if (StringUtils.isBlank(type)) {
            list.add("NO TYPE");
        } else if (StringUtils.isBlank(suggestion)) {
            list.add(type);
            assert StringUtils.isNotBlank(subtype);
            list.add(subtype);
        } else {
            assert ReplacementType.CUSTOM.getLabel().equals(type);
            list.add(type);
            assert StringUtils.isNotBlank(subtype);
            list.add(subtype);
            list.add(suggestion);
            assert cs != null;
            list.add(Boolean.toString(cs));
        }

        return StringUtils.join(list, " - ");
    }
}
