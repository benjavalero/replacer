package es.bvalero.replacer.page.index;

import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.common.domain.WikipediaPageId;
import es.bvalero.replacer.finder.replacement.Replacement;
import es.bvalero.replacer.page.repository.ReplacementModel;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;

@UtilityClass
class IndexableReplacementMapper {

    List<ReplacementModel> toModel(Collection<IndexableReplacement> replacements) {
        return replacements.stream().map(IndexableReplacementMapper::toModel).collect(Collectors.toUnmodifiableList());
    }

    private ReplacementModel toModel(IndexableReplacement replacement) {
        return ReplacementModel
            .builder()
            .id(replacement.getId())
            .lang(replacement.getIndexablePageId().getLang())
            .pageId(replacement.getIndexablePageId().getPageId())
            .type(replacement.getType())
            .subtype(replacement.getSubtype())
            .position(replacement.getPosition())
            .context(replacement.getContext())
            .lastUpdate(replacement.getLastUpdate())
            .reviewer(replacement.getReviewer())
            .build();
    }

    List<IndexableReplacement> fromModel(Collection<ReplacementModel> replacements) {
        return replacements
            .stream()
            .map(IndexableReplacementMapper::fromModel)
            .collect(Collectors.toUnmodifiableList());
    }

    private IndexableReplacement fromModel(ReplacementModel replacement) {
        return IndexableReplacement
            .builder()
            .id(replacement.getId())
            .indexablePageId(WikipediaPageId.of(replacement.getLang(), replacement.getPageId()))
            .type(replacement.getType())
            .subtype(replacement.getSubtype())
            .position(replacement.getPosition())
            .context(replacement.getContext())
            .lastUpdate(replacement.getLastUpdate())
            .reviewer(replacement.getReviewer())
            .build();
    }

    IndexableReplacement fromDomain(Replacement replacement, WikipediaPage page) {
        return IndexableReplacement
            .builder()
            .indexablePageId(page.getId())
            .type(replacement.getType().getLabel())
            .subtype(replacement.getSubtype())
            .position(replacement.getStart())
            .context(replacement.getContext(page.getContent()))
            .lastUpdate(page.getLastUpdate().toLocalDate())
            .build();
    }
}
