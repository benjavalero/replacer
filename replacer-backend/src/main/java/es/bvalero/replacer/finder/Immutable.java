package es.bvalero.replacer.finder;

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

    boolean contains(Replacement r) {
        // i (this) contains r if: startI <= startR < endR <= endI
        return this.getStart() <= r.getStart() && r.getEnd() <= this.getEnd();
    }

    void check(IndexablePage page) {
        this.finder.checkMaxLength(this, page, LOGGER);
    }
}
