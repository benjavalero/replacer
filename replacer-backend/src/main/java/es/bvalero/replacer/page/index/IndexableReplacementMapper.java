package es.bvalero.replacer.page.index;

import es.bvalero.replacer.common.domain.*;
import es.bvalero.replacer.repository.ReplacementModel;
import java.util.Collection;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;

@UtilityClass
class IndexableReplacementMapper {

    Collection<ReplacementModel> toModel(Collection<IndexableReplacement> replacements) {
        return replacements.stream().map(IndexableReplacementMapper::toModel).collect(Collectors.toUnmodifiableList());
    }

    private ReplacementModel toModel(IndexableReplacement replacement) {
        return ReplacementModel
            .builder()
            .id(replacement.getId())
            .lang(replacement.getPageId().getLang().getCode())
            .pageId(replacement.getPageId().getPageId())
            .kind(replacement.getType().getKind().getCode())
            .subtype(replacement.getType().getSubtype())
            .start(replacement.getStart())
            .context(replacement.getContext())
            .reviewer(replacement.getReviewer())
            .build();
    }

    Collection<IndexableReplacement> fromModel(Collection<ReplacementModel> replacements) {
        return replacements
            .stream()
            .map(IndexableReplacementMapper::fromModel)
            .collect(Collectors.toUnmodifiableList());
    }

    private IndexableReplacement fromModel(ReplacementModel replacement) {
        // The context in database can be null as there are thousands of reviewed cases before the context existed.
        // As a trick we fake the context so the one in the IndexableReplacement can be still not-null.
        return IndexableReplacement
            .builder()
            .id(replacement.getId())
            .pageId(WikipediaPageId.of(WikipediaLanguage.valueOfCode(replacement.getLang()), replacement.getPageId()))
            .type(replacement.getType())
            .start(replacement.getStart())
            .context(replacement.getContext())
            .reviewer(replacement.getReviewer())
            .build();
    }

    IndexableReplacement fromDomain(Replacement replacement, WikipediaPage page) {
        return IndexableReplacement
            .builder()
            .pageId(page.getId())
            .type(replacement.getType())
            .start(replacement.getStart())
            .context(replacement.getContext(page))
            .build();
    }
}
