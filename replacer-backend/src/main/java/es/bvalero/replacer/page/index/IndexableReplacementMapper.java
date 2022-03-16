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
            .lang(replacement.getIndexablePageId().getLang().getCode())
            .pageId(replacement.getIndexablePageId().getPageId())
            .kind(replacement.getType().getKind().getCode())
            .subtype(replacement.getType().getSubtype())
            .position(replacement.getPosition())
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
            .indexablePageId(
                IndexablePageId.of(WikipediaLanguage.valueOfCode(replacement.getLang()), replacement.getPageId())
            )
            .type(ReplacementType.of(replacement.getKind(), replacement.getSubtype()))
            .position(replacement.getPosition())
            .context(replacement.getContext())
            .reviewer(replacement.getReviewer())
            .build();
    }

    IndexableReplacement fromDomain(Replacement replacement, WikipediaPage page) {
        return IndexableReplacement
            .builder()
            .indexablePageId(IndexablePageMapper.fromDomain(page.getId()))
            .type(replacement.getType())
            .position(replacement.getStart())
            .context(replacement.getContext(page))
            .build();
    }
}
