package es.bvalero.replacer.article;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

@Value
class ReplacementCountList implements Comparable<ReplacementCountList> {

    @JsonProperty("t")
    private String type;
    @JsonProperty("l")
    private Collection<ReplacementCount> counts;

    @Override
    public int compareTo(@NotNull ReplacementCountList list) {
        return this.type.compareTo(list.type);
    }

}
