package es.bvalero.replacer.common.domain;

import es.bvalero.replacer.finder.replacement.ReplacementType;
import java.util.Collection;
import java.util.Objects;
import lombok.Builder;
import lombok.Value;
import lombok.With;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;

/**
 * Replacement found in the content of a page.
 *
 * A <strong>replacement</strong> is a potential issue to be checked and fixed (replaced). For instance,
 * the word "aproximated" is misspelled and therefore could be proposed to be replaced with "approximated".
 *
 * Note the importance of the <em>potential</em> adjective, as an issue could be just a false positive.
 * For instance, in Spanish the word "Paris" could be misspelled if it corresponds to the French city
 * (written correctly as "París"), but it would be correct if it refers to the mythological Trojan prince.
 */
@Value
@Builder
public class Replacement implements Comparable<Replacement> {

    private static final int MAX_SUBTYPE_LENGTH = 100; // Constrained by the database

    @With
    @NonNull
    Integer start;

    @NonNull
    String text;

    @NonNull
    ReplacementType type;

    @NonNull
    String subtype;

    @NonNull
    Collection<Suggestion> suggestions;

    private Replacement(
        Integer start,
        String text,
        ReplacementType type,
        String subtype,
        Collection<Suggestion> suggestions
    ) {
        // Validate start
        if (start < 0) {
            throw new IllegalArgumentException("Negative replacement start: " + start);
        }

        // Validate text
        if (StringUtils.isBlank(text)) {
            throw new IllegalArgumentException("Blank replacement text: " + start);
        }

        // Validate subtype
        if (subtype.length() > MAX_SUBTYPE_LENGTH) {
            throw new IllegalArgumentException("Too long replacement subtype: " + subtype);
        }

        // There must exist at least a suggestion different from the found text
        if (suggestions.isEmpty() || suggestions.stream().allMatch(s -> s.getText().equals(text))) {
            throw new IllegalArgumentException("Invalid replacement suggestions");
        }

        this.start = start;
        this.text = text;
        this.type = type;
        this.subtype = subtype;
        this.suggestions = suggestions;
    }

    public int getEnd() {
        return this.getStart() + this.getText().length();
    }

    @Override
    public int compareTo(Replacement o) {
        // Order descendant by start. If equals, the lower end.
        return Objects.equals(o.getStart(), getStart()) ? getEnd() - o.getEnd() : o.getStart() - getStart();
    }
}
