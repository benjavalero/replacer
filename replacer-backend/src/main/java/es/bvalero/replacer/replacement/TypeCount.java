package es.bvalero.replacer.replacement;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

@Value(staticConstructor = "of")
class TypeCount implements Comparable<TypeCount> {

    @JsonProperty("t")
    private String type;
    @JsonProperty("l")
    private Collection<SubtypeCount> subtypeCounts;

    @Override
    public int compareTo(@NotNull TypeCount list) {
        return this.type.compareTo(list.type);
    }

}
