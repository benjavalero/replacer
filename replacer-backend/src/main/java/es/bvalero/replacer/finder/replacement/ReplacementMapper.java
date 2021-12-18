package es.bvalero.replacer.finder.replacement;

import es.bvalero.replacer.common.domain.PageReplacement;
import java.util.Collection;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ReplacementMapper {

    public Collection<PageReplacement> toDomain(Collection<Replacement> replacements) {
        return replacements.stream().map(ReplacementMapper::toDomain).collect(Collectors.toUnmodifiableList());
    }

    private PageReplacement toDomain(Replacement replacement) {
        return PageReplacement
            .builder()
            .start(replacement.getStart())
            .text(replacement.getText())
            .type(replacement.getType())
            .suggestions(replacement.getSuggestions())
            .build();
    }
}
