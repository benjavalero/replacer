package es.bvalero.replacer.article;

import lombok.Value;
import lombok.experimental.NonFinal;

@Value
class ReplacementCount {

    private String type;
    private String subtype;
    @NonFinal
    private long count;

    void decrementCount(int n) {
        this.count = Math.max(0, this.count - n);
    }

}
