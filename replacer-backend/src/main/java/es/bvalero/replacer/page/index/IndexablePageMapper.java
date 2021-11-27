package es.bvalero.replacer.page.index;

import es.bvalero.replacer.common.domain.Replacement;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.common.domain.WikipediaPageId;
import es.bvalero.replacer.page.repository.PageModel;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;

@UtilityClass
class IndexablePageMapper {

    List<PageModel> toModel(Collection<IndexablePage> pages) {
        return pages.stream().map(IndexablePageMapper::toModel).collect(Collectors.toUnmodifiableList());
    }

    private PageModel toModel(IndexablePage page) {
        return PageModel
            .builder()
            .lang(page.getId().getLang())
            .pageId(page.getId().getPageId())
            .title(page.getTitle())
            .replacements(IndexableReplacementMapper.toModel(page.getReplacements()))
            .build();
    }

    IndexablePage fromModel(PageModel page) {
        return IndexablePage
            .builder()
            .id(IndexablePageId.of(page.getLang(), page.getPageId()))
            .title(page.getTitle())
            .replacements(IndexableReplacementMapper.fromModel(page.getReplacements()))
            .build();
    }

    IndexablePage fromDomain(WikipediaPage page, Collection<Replacement> replacements) {
        return IndexablePage
            .builder()
            .id(fromDomain(page.getId()))
            .title(page.getTitle())
            .lastUpdate(page.getLastUpdate().toLocalDate())
            .replacements(
                replacements
                    .stream()
                    .map(r -> IndexableReplacementMapper.fromDomain(r, page))
                    .collect(Collectors.toList())
            )
            .build();
    }

    IndexablePageId fromDomain(WikipediaPageId id) {
        return IndexablePageId.of(id.getLang(), id.getPageId());
    }
}
