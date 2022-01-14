package es.bvalero.replacer.common.domain;

import lombok.Builder;
import lombok.ToString;
import lombok.Value;
import org.springframework.lang.NonNull;

/** Section in a Wikipedia page (but its content) */
@Value
@Builder
public class WikipediaSection implements Comparable<WikipediaSection> {

    @ToString.Exclude
    int level;

    int index;

    @ToString.Exclude
    int byteOffset;

    @NonNull
    String anchor;

    @Override
    public int compareTo(WikipediaSection other) {
        return Integer.compare(byteOffset, other.byteOffset);
    }
}
