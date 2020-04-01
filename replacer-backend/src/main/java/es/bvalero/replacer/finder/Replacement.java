package es.bvalero.replacer.finder;

import java.util.List;
import java.util.Objects;
import lombok.Builder;
import lombok.Value;
import lombok.With;

/**
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
    /**
     * The category of the replacement: misspelling, date format, etc.
     * It may include a subtype, for instance the particular misspelling.
     */
    private String type;
    private String subtype;

    /** The start position of the section in the text */
    private int start;

    /**
     * Optionally, the text in the section, especially for debugging purposes,
     * i. e. the text between the start and end position of the section.
     */
    private String text;

    /** At least one suggestion to replace the text */
    private List<Suggestion> suggestions;

    /** The snippet around the text to replace */
    @With
    private String context;

    /** The end position of the section in the text */
    public int getEnd() {
        return this.start + this.text.length();
    }

    @Override
    public boolean equals(final Object o) {
        // Two replacements are equal if they have the same start and end
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Replacement that = (Replacement) o;
        return start == that.start && getEnd() == that.getEnd();
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, getEnd());
    }

    @Override
    public int compareTo(Replacement o) {
        // Order descendant by start. If equals, the lower end.
        return o.start == start ? getEnd() - o.getEnd() : o.start - start;
    }

    public boolean contains(Replacement r) {
        // i (this) contains r if: startI < startR < endR < endI
        // Also if one limit is strict and the other not
        return (
            (this.getStart() < r.getStart() && r.getEnd() < this.getEnd()) ||
            (this.getStart() <= r.getStart() && r.getEnd() < this.getEnd()) ||
            (this.getStart() < r.getStart() && r.getEnd() <= this.getEnd())
        );
    }
}
