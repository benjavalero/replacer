package es.bvalero.replacer.article;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Value;
import lombok.experimental.NonFinal;

@Data
class SubtypeCount {

    @JsonProperty("s")
    private String subtype;
    @JsonProperty("c")
    private long count;

    void decrementCount(int n) {
        this.count = Math.max(0, this.count - n);
    }

}
