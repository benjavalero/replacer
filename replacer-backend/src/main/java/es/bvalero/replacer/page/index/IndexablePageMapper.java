package es.bvalero.replacer.page.index;

import es.bvalero.replacer.common.domain.WikipediaPageId;
import es.bvalero.replacer.page.repository.PageModel;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.lang.Nullable;

public class IndexablePageMapper {

    static List<PageModel> toModel(Collection<IndexablePage> pages) {
        return pages.stream().map(IndexablePageMapper::toModel).collect(Collectors.toUnmodifiableList());
    }

    private static PageModel toModel(IndexablePage page) {
        return PageModel
            .builder()
            .lang(page.getId().getLang())
            .pageId(page.getId().getPageId())
            .title(page.getTitle())
            .replacements(IndexableReplacementMapper.toModel(page.getReplacements()))
            .build();
    }

    @Nullable
    public static IndexablePage fromModel(@Nullable PageModel page) {
        if (Objects.isNull(page)) {
            return null;
        }
        return IndexablePage
            .builder()
            .id(WikipediaPageId.of(page.getLang(), page.getPageId()))
            .title(page.getTitle())
            .replacements(IndexableReplacementMapper.fromModel(page.getReplacements()))
            .build();
    }
}
