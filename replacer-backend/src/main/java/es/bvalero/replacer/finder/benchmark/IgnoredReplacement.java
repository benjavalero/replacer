package es.bvalero.replacer.finder.benchmark;

import lombok.Value;

/**
 * An <strong>immutable</strong> is a section in the page contents to be left untouched,
 * for instance a literal quote, so any replacement found within it must be ignored
 * and not offered to the user for revision.
 */
@Value(staticConstructor = "of")
public class IgnoredReplacement {
    /** The start position of the section in the page contents */
    private int start;

    /**
     * Optionally, the text in the section, especially for debugging purposes,
     * i. e. the text between the start and end position of the section.
     */
    private String text;

    /** The end position of the section in the page contents */
    int getEnd() {
        return this.start + this.text.length();
    }
}
