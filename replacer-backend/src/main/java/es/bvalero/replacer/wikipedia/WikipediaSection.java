package es.bvalero.replacer.wikipedia;

import lombok.Builder;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

@Value
@Builder
public class WikipediaSection implements Comparable<WikipediaSection> {
    private int level;
    private int index;
    private int byteOffset;

    @Override
    public int compareTo(@NotNull WikipediaSection other) {
        return Integer.compare(byteOffset, other.byteOffset);
    }

}
