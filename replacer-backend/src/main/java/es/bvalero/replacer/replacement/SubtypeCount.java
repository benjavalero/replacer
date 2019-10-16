package es.bvalero.replacer.replacement;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value(staticConstructor = "of")
class SubtypeCount {

    @JsonProperty("s")
    private String subtype;
    @JsonProperty("c")
    private long count;

}
