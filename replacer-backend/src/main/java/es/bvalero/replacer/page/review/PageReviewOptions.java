package es.bvalero.replacer.page.review;

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

    // TODO: Remove the lang and user common query parameters
    // In fact we should split this object into a DTO and a Domain one

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

    @TestOnly
    public static PageReviewOptions ofNoType() {
        return PageReviewOptions.builder().lang(WikipediaLanguage.getDefault()).user("").build();
    }

    @TestOnly
    public static PageReviewOptions ofTypeSubtype(String type, String subtype) {
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

    public PageReviewOptionsType getOptionsType() {
        if (StringUtils.isBlank(type)) {
            return PageReviewOptionsType.NO_TYPE;
        } else if (StringUtils.isBlank(suggestion)) {
            return PageReviewOptionsType.TYPE_SUBTYPE;
        } else {
            return PageReviewOptionsType.CUSTOM;
        }
    }

    @Override
    public String toString() {
        return String.format("%s - %s - %s", user, lang.toString(), toStringSearchType());
    }

    String toStringSearchType() {
        List<String> list = new ArrayList<>();
        switch (getOptionsType()) {
            case NO_TYPE:
                list.add("NO TYPE");
                break;
            case TYPE_SUBTYPE:
                list.add(type);
                list.add(subtype);
                break;
            case CUSTOM:
                list.add(type);
                list.add(subtype);
                list.add(suggestion);
                list.add(Boolean.toString(Objects.requireNonNull(cs)));
                break;
        }

        return StringUtils.join(list, " - ");
    }

    boolean isValid() {
        boolean isValid = false;
        switch (getOptionsType()) {
            case NO_TYPE:
                isValid = type == null && subtype == null && suggestion == null && cs == null;
                break;
            case TYPE_SUBTYPE:
                isValid =
                    StringUtils.isNotBlank(type) && StringUtils.isNotBlank(subtype) && suggestion == null && cs == null;
                break;
            case CUSTOM:
                isValid =
                    ReplacementType.CUSTOM.getLabel().equals(type) &&
                    StringUtils.isNotBlank(subtype) &&
                    StringUtils.isNotBlank(suggestion) &&
                    cs != null;
                break;
        }
        return isValid;
    }
}
