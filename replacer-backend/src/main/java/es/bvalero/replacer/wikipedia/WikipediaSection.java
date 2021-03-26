package es.bvalero.replacer.wikipedia;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class WikipediaSection implements Comparable<WikipediaSection> {

    int level;
    int index;
    int byteOffset;
    String anchor;

    @Override
    public int compareTo(WikipediaSection other) {
        return Integer.compare(byteOffset, other.byteOffset);
    }

    @Override
    public String toString() {
        return "WikipediaSection(index=" + this.getIndex() + ", anchor=" + this.getAnchor() + ")";
    }
}
