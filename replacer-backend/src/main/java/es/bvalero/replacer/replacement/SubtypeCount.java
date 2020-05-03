package es.bvalero.replacer.replacement;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
class SubtypeCount {
    @JsonProperty("s")
    private final String subtype;

    @Setter
    @JsonProperty("c")
    private long count;
}
