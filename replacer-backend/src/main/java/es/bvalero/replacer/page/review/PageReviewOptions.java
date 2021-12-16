package es.bvalero.replacer.page.review;

import com.fasterxml.jackson.annotation.JsonIgnore;
import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.TestOnly;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.lang.Nullable;

@ParameterObject
@PageReviewOptionsValid
@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class PageReviewOptions {

    // TODO: Remove the lang and user common query parameters
    // In fact we should split this object into a DTO and a Domain one and validate the Domain one

    @Parameter(
        description = "Language of the Wikipedia in use",
        schema = @Schema(type = "string", allowableValues = { "es", "gl" }),
        required = true
    )
    @NotNull
    private String lang;

    @Parameter(description = "Name of the user in Wikipedia", required = true, example = "Benjavalero")
    @Size(max = 100)
    @NotBlank
    private String user;

    @Parameter(description = "Replacement type", example = "Ortografía")
    @Nullable
    private String type; // TODO: ReplacementType

    @Size(max = 100)
    @Parameter(description = "Replacement subtype", example = "aún")
    @Nullable
    private String subtype;

    @Parameter(description = "Custom replacement suggestion", example = "todavía")
    @Nullable
    private String suggestion;

    @Parameter(description = "If the custom replacement is case-sensitive", example = "false")
    @Nullable
    private Boolean cs;

    public WikipediaLanguage getWikipediaLanguage() {
        return WikipediaLanguage.valueOfCode(lang);
    }

    @Nullable
    public ReplacementType getReplacementType() {
        return Objects.nonNull(type) && Objects.nonNull(subtype)
            ? ReplacementType.of(ReplacementKind.valueOfLabel(type), subtype)
            : null;
    }

    @TestOnly
    public static PageReviewOptions ofNoType() {
        return PageReviewOptions.builder().lang(WikipediaLanguage.getDefault().getCode()).user("A").build();
    }

    @TestOnly
    public static PageReviewOptions ofType(ReplacementType type) {
        return PageReviewOptions
            .builder()
            .lang(WikipediaLanguage.getDefault().getCode())
            .user("A")
            .type(type.getKind().getLabel())
            .subtype(type.getSubtype())
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
            .lang(lang.getCode())
            .user("A")
            .type(ReplacementKind.CUSTOM.getLabel())
            .subtype(replacement)
            .suggestion(suggestion)
            .cs(caseSensitive)
            .build();
    }

    @JsonIgnore
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
        return String.format("%s - %s - %s", user, lang, toStringSearchType());
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
                    ReplacementKind.CUSTOM.getLabel().equals(type) &&
                    StringUtils.isNotBlank(subtype) &&
                    StringUtils.isNotBlank(suggestion) &&
                    cs != null;
                break;
        }
        return isValid;
    }
}
