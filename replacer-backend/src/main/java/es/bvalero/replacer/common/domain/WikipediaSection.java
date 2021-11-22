package es.bvalero.replacer.common.domain;

import lombok.Builder;
import lombok.ToString;
import lombok.Value;
import org.springframework.lang.NonNull;

/** Domain object representing the hierarchy of a section in a Wikipedia page */
@Value
@Builder
public class WikipediaSection implements Comparable<WikipediaSection> {

    @ToString.Exclude
    @NonNull
    Integer level;

    @NonNull
    Integer index;

    @ToString.Exclude
    @NonNull
    Integer byteOffset;

    @NonNull
    String anchor;

    @Override
    public int compareTo(WikipediaSection other) {
        return Integer.compare(byteOffset, other.byteOffset);
    }
}
