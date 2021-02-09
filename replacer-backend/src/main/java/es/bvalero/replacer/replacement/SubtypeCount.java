package es.bvalero.replacer.replacement;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Value(staticConstructor = "of")
class SubtypeCount {

    @JsonProperty("s")
    String subtype;

    @JsonProperty("c")
    @With
    long count;
}
