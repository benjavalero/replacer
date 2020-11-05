package es.bvalero.replacer.wikipedia;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WikipediaSection implements Comparable<WikipediaSection> {
    int level;
    int index;
    int byteOffset;
    String anchor;

    @Override
    public int compareTo(@NotNull WikipediaSection other) {
        return Integer.compare(byteOffset, other.byteOffset);
    }
}
