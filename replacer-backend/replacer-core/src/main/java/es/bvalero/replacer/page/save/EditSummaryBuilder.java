package es.bvalero.replacer.page.save;

import es.bvalero.replacer.finder.ReplacementType;
import java.util.Collection;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

/** Utility class to build the summary for a Wikipedia edit based on the fixed replacement types */
@UtilityClass
class EditSummaryBuilder {

    private static final String EDIT_SUMMARY = "Reemplazos con [[Usuario:Benjavalero/Replacer|Replacer]]";
    private static final String COSMETIC_CHANGES = "mejoras cosméticas";

    String build(Collection<ReplacementType> fixedReplacementTypes, boolean applyCosmetics) {
        if (fixedReplacementTypes.isEmpty()) {
            throw new IllegalArgumentException("No fixed replacements when building edit summary");
        }

        // The summary is truncated to 500 codepoints when the page is published
        // https://en.wikipedia.org/wiki/Help:Edit_summary#The_500-character_limit
        Collection<String> fixed = fixedReplacementTypes
            .stream()
            .map(EditSummaryBuilder::buildSubtypeSummary)
            .collect(Collectors.toUnmodifiableSet());
        StringBuilder summary = new StringBuilder(EDIT_SUMMARY).append(": ").append(StringUtils.join(fixed, ", "));

        if (applyCosmetics) {
            summary.append(" + ").append(COSMETIC_CHANGES);
        }
        return summary.toString();
    }

    private String buildSubtypeSummary(ReplacementType type) {
        return switch (type.getKind()) {
            case SIMPLE, COMPOSED, CUSTOM -> "«" + type.getSubtype() + "»";
            case EMPTY -> throw new IllegalArgumentException();
            default -> type.getSubtype();
        };
    }
}
