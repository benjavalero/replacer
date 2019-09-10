package es.bvalero.replacer.article;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;
import lombok.experimental.NonFinal;

@Value
class ReplacementCount {

    @JsonIgnore
    private String type;
    @JsonProperty("s")
    private String subtype;
    @NonFinal
    @JsonProperty("c")
    private long count;

    void decrementCount(int n) {
        this.count = Math.max(0, this.count - n);
    }

}
