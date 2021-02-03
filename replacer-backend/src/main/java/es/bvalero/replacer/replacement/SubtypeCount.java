package es.bvalero.replacer.replacement;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Value
class SubtypeCount {

    @JsonProperty("s")
    String subtype;

    @JsonProperty("c")
    @With
    long count;
}
