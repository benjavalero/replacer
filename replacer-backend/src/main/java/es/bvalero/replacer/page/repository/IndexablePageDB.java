package es.bvalero.replacer.page.repository;

import es.bvalero.replacer.domain.WikipediaLanguage;
import es.bvalero.replacer.replacement.ReplacementEntity;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.springframework.lang.Nullable;

// TODO: This is a temporary class until it is unified with the existing IndexablePage

@Value
@Builder
public class IndexablePageDB {

    @NonNull
    WikipediaLanguage lang;

    @NonNull
    Integer id;

    // TODO: This should be NonNull. To check in Production DB if there is still any null case.
    @Nullable
    String title;

    @NonNull
    List<IndexableReplacementDB> replacements;

    // TODO: Remove once we unify with ReplacementEntity
    public List<ReplacementEntity> convert() {
        return this.getReplacements().stream().map(this::convert).collect(Collectors.toList());
    }

    private ReplacementEntity convert(IndexableReplacementDB replacement) {
        return ReplacementEntity
            .builder()
            .id(replacement.getId())
            .lang(this.getLang().getCode())
            .pageId(this.getId())
            .type(replacement.getType())
            .subtype(replacement.getSubtype())
            .position(replacement.getPosition())
            .context(replacement.getContext())
            .lastUpdate(replacement.getLastUpdate())
            .reviewer(replacement.getReviewer())
            .title(this.getTitle())
            .build();
    }
}
