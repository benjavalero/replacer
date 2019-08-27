package es.bvalero.replacer.wikipedia;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class WikipediaSection {
    private int tocLevel;
    private int level;
    private String line;
    private String number;
    private int index;
    private int byteOffset;
}
