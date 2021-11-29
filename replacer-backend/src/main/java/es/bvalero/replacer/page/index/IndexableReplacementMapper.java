package es.bvalero.replacer.page.index;

import es.bvalero.replacer.common.domain.Replacement;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.page.repository.ReplacementModel;
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

    Collection<IndexableReplacement> fromModel(Collection<ReplacementModel> replacements) {
        return replacements
            .stream()
            .map(IndexableReplacementMapper::fromModel)
            .collect(Collectors.toUnmodifiableList());
    }

    private IndexableReplacement fromModel(ReplacementModel replacement) {
        return IndexableReplacement
            .builder()
            .id(replacement.getId())
            .indexablePageId(IndexablePageId.of(replacement.getLang(), replacement.getPageId()))
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
            .indexablePageId(IndexablePageMapper.fromDomain(page.getId()))
            .type(replacement.getType().getLabel())
            .subtype(replacement.getSubtype())
            .position(replacement.getStart())
            .context(getContext(replacement, page))
            .lastUpdate(page.getLastUpdate().toLocalDate())
            .build();
    }

    private String getContext(Replacement replacement, WikipediaPage page) {
        return FinderUtils.getContextAroundWord(page.getContent(), replacement.getStart(), replacement.getEnd(), 20);
    }
}
