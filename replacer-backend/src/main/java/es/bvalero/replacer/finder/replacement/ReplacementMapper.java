package es.bvalero.replacer.finder.replacement;

import java.util.Collection;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ReplacementMapper {

    public Collection<es.bvalero.replacer.common.domain.Replacement> toDomain(Collection<Replacement> replacements) {
        return replacements.stream().map(ReplacementMapper::toDomain).collect(Collectors.toUnmodifiableList());
    }

    private es.bvalero.replacer.common.domain.Replacement toDomain(Replacement replacement) {
        return es.bvalero.replacer.common.domain.Replacement
            .builder()
            .start(replacement.getStart())
            .text(replacement.getText())
            .type(replacement.getType())
            .suggestions(replacement.getSuggestions())
            .build();
    }
}
