package es.bvalero.replacer.finder;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.TestOnly;

/**
 * An <strong>immutable</strong> is a section in the page contents to be left untouched,
 * for instance a literal quote, so any replacement found within it must be ignored
 * and not offered to the user for revision.
 */
@Getter
@Accessors(fluent = true)
@EqualsAndHashCode(of = { "start", "end" })
@ToString
public class Immutable implements FinderResult {

    private final String pageContent;
    private final int start;
    private final int end;
    private String text;

    private Immutable(String pageContent, int start, int end, String text) {
        this.pageContent = pageContent;
        this.start = start;
        this.end = end;
        this.text = text;
    }

    @Override
    public String text() {
        if (text == null) {
            text = pageContent.substring(start, end);
        }
        return text;
    }

    public static Immutable of(String pageContent, int start, int end) {
        return new Immutable(pageContent, start, end, null);
    }

    @TestOnly
    public static Immutable of(String pageContent, int start, String text) {
        return new Immutable(pageContent, start, start + text.length(), text);
    }
}
