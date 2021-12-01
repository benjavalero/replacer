package es.bvalero.replacer.page.review;

import com.fasterxml.jackson.annotation.JsonIgnore;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.replacement.ReplacementType;
import io.swagger.annotations.ApiParam;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.TestOnly;
import org.springframework.lang.Nullable;

@PageReviewOptionsValid
@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class PageReviewOptions {

    @ApiParam(value = "Language", required = true)
    @NotNull
    private WikipediaLanguage lang;

    @ApiParam(value = "Wikipedia user name", required = true, example = "Benjavalero")
    @NotNull
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
    Integer offset = null; // TODO: Check what this is used for and maybe move to Custom Page Review

    void incrementOffset(int increment) {
        this.offset = this.offset == null ? 0 : this.offset + increment;
    }

    @TestOnly
    static PageReviewOptions ofNoType() {
        return PageReviewOptions.builder().lang(WikipediaLanguage.getDefault()).user("").build();
    }

    @TestOnly
    static PageReviewOptions ofTypeSubtype(String type, String subtype) {
        return PageReviewOptions
            .builder()
            .lang(WikipediaLanguage.getDefault())
            .user("")
            .type(type)
            .subtype(subtype)
            .build();
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
            .user("")
            .type(ReplacementType.CUSTOM.getLabel())
            .subtype(replacement)
            .suggestion(suggestion)
            .cs(caseSensitive)
            .build();
    }

    @Override
    public String toString() {
        return String.format("%s - %s - %s", user, lang.toString(), toStringSearchType());
    }

    String toStringSearchType() {
        List<String> list = new ArrayList<>();
        if (StringUtils.isBlank(type)) {
            list.add("NO TYPE");
        } else if (StringUtils.isBlank(suggestion)) {
            list.add(type);
            list.add(subtype);
        } else {
            list.add(type);
            list.add(subtype);
            list.add(suggestion);
            list.add(Boolean.toString(Objects.requireNonNull(cs)));
        }

        return StringUtils.join(list, " - ");
    }

    boolean isValid() {
        if (StringUtils.isBlank(type)) {
            // No type
            return type == null && subtype == null && suggestion == null && cs == null;
        } else if (StringUtils.isBlank(suggestion)) {
            // Type-subtype
            return StringUtils.isNotBlank(type) && StringUtils.isNotBlank(subtype) && cs == null;
        } else {
            // Custom
            return ReplacementType.CUSTOM.getLabel().equals(type) && StringUtils.isNotBlank(subtype) && cs != null;
        }
    }
}
