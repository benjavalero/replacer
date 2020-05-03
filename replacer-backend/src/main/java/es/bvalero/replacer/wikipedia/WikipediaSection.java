package es.bvalero.replacer.wikipedia;

import lombok.Builder;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

@Value
@Builder
public class WikipediaSection implements Comparable<WikipediaSection> {
    int level;
    int index;
    int byteOffset;

    @Override
    public int compareTo(@NotNull WikipediaSection other) {
        return Integer.compare(byteOffset, other.byteOffset);
    }
}
