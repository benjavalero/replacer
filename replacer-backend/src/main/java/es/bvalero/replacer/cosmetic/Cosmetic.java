package es.bvalero.replacer.cosmetic;

import lombok.Value;

/**
 * A <strong>cosmetic</strong> is a section in the page contents that can be fixed automatically.
 */
@Value(staticConstructor = "of")
class Cosmetic implements Comparable<Cosmetic> {
    /** The start position of the section in the text */
    private final int start;

    /** The text in the section to be fixed */
    private final String text;

    /** The fix for the found text */
    private final String fix;

    /** The end position of the section in the text */
    public int getEnd() {
        return this.start + this.text.length();
    }

    @Override
    public int compareTo(Cosmetic o) {
        // Order descendant by start. If equals, the lower end.
        return o.start == start ? getEnd() - o.getEnd() : o.start - start;
    }
}
