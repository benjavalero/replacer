package es.bvalero.replacer.wikipedia;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
class WikipediaSection {
    private int tocLevel;
    private String level;
    private String line;
    private String number;
    private String index;
    private int byteOffset;
}
