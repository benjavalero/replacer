package es.bvalero.replacer.finder;

import es.bvalero.replacer.finder.misspelling.MisspellingComposedFinder;
import es.bvalero.replacer.page.IndexablePage;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

/**
 * An <strong>immutable</strong> is a section in the page contents to be left untouched,
 * for instance a literal quote, so any replacement found within it must be ignored
 * and not offered to the user for revision.
 */
@Slf4j
@Value(staticConstructor = "of")
public class Immutable {

    /**
     * The start position of the section in the text
     */
    int start;

    /**
     * Optionally, the text in the section, especially for debugging purposes,
     * i.e. the text between the start and end position of the section.
     */
    String text;

    /**
     * The finder which has found it.
     */
    ImmutableFinder finder;

    /**
     * The end position of the section in the text
     */
    public int getEnd() {
        return this.start + this.text.length();
    }

    private boolean contains(Replacement r) {
        // i (this) contains r if: startI <= startR < endR <= endI
        return this.getStart() <= r.getStart() && r.getEnd() <= this.getEnd();
    }

    private boolean intersects(Replacement r) {
        // i (this) intersect r on the left  if: startI <= startR < endI
        // i (this) intersect r on the right if: startI < endR <= endI
        return (
            (this.getStart() <= r.getStart() && r.getStart() < this.getEnd()) ||
            (this.getStart() < r.getEnd() && r.getEnd() <= this.getEnd())
        );
    }

    boolean containsOrIntersects(Replacement r) {
        // For type Ortografía we just check a simple contains
        return MisspellingComposedFinder.TYPE_MISSPELLING_COMPOSED.equals(r.getType())
            ? this.intersects(r)
            : this.contains(r);
    }

    void check(IndexablePage page) {
        this.finder.checkMaxLength(this, page, LOGGER);
    }
}
