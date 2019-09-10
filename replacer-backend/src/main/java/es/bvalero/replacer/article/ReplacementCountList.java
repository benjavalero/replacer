package es.bvalero.replacer.article;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import java.util.Collection;

@Value
class ReplacementCountList {

    @JsonProperty("t")
    private String type;
    @JsonProperty("l")
    private Collection<ReplacementCount> counts;

}
