package es.bvalero.replacer.finder;

import java.util.List;
import lombok.Builder;
import lombok.Value;

/**
 * A <strong>replacement</strong> is a potential issue to be checked and fixed (replaced). For instance,
 * the word "aproximated" is misspelled and therefore could be proposed to be replaced with "approximated".
 *
 * Note the importance of the <em>potential</em> adjective, as an issue could be just a false positive.
 * For instance, in Spanish the word "Paris" could be misspelled if it corresponds to the French city
 * (written correctly as "París"), but it would be correct if it refers to the mythological Trojan prince.
 */
@Value(staticConstructor = "of")
@Builder
public class Replacement implements Comparable<Replacement> {
    /**
     * The category of the replacement: misspelling, date format, etc.
     * It may include a subtype, for instance the particular misspelling.
     */
    private String type;
    private String subtype;

    /** A number corresponding to the position in the page contents where the text is found */
    private int start;

    //** The text to be checked and fixed. It can be a word or an expression. */
    private String text;

    /** At least one suggestion to replace the text */
    private List<Suggestion> suggestions;

    public int getEnd() {
        return this.start + this.text.length();
    }

    @Override
    public int compareTo(Replacement o) {
        // Order descendant by start. If equals, the lower end.
        return o.start == start ? getEnd() - o.getEnd() : o.start - start;
    }
}
