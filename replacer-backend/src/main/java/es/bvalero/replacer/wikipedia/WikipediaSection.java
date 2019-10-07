package es.bvalero.replacer.wikipedia;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class WikipediaSection {
    private int level;
    private int index;
    private int byteOffset;
}
