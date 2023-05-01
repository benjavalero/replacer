package es.bvalero.replacer.wikipedia;

import es.bvalero.replacer.page.PageKey;
import lombok.Builder;
import lombok.ToString;
import lombok.Value;
import org.springframework.lang.NonNull;

/**
 * A Wikipedia section representing just the properties of a section in a Wikipedia page,
 * like level or anchor title, without the page section content.
 * A section can be identified by its index and the page it belongs to.
 * A section is always related to a Wikipedia page and cannot exist without it.
 */
@Value
@Builder
public class WikipediaSection implements Comparable<WikipediaSection> {

    /* ID of the Wikipedia page the section belongs to */
    @NonNull
    PageKey pageKey;

    /* Index of the section, starting by 1. */
    int index;

    /* Level of the section, usually starting by 2, as 1 is the root level. */
    @ToString.Exclude
    int level;

    /*
     * The distance in bytes from the beginning of the page.
     * It is usually equal to the position of the section in the page content text,
     * but it can be different in some cases containing Unicode characters like emojis.
     */
    @ToString.Exclude
    int byteOffset;

    /* The section anchor is the title we can link to in a browser */
    @NonNull
    String anchor;

    @Override
    public int compareTo(WikipediaSection other) {
        return Integer.compare(this.byteOffset, other.byteOffset);
    }
}
