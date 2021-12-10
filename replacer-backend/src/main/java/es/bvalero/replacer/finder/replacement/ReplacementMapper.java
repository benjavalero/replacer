package es.bvalero.replacer.finder.replacement;

import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.Suggestion;
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
            .type(ReplacementType.of(replacement.getType(), replacement.getSubtype()))
            .suggestions(toSuggestion(replacement.getSuggestions()))
            .build();
    }

    private Collection<Suggestion> toSuggestion(Collection<ReplacementSuggestion> suggestions) {
        return suggestions.stream().map(ReplacementMapper::toSuggestion).collect(Collectors.toUnmodifiableList());
    }

    private Suggestion toSuggestion(ReplacementSuggestion suggestion) {
        return Suggestion.of(suggestion.getText(), suggestion.getComment());
    }
}
