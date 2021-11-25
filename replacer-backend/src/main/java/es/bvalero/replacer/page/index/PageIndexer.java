package es.bvalero.replacer.page.index;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Component
public class PageIndexer {

    public PageIndexResult indexPageReplacements(@Nullable IndexablePage page, @Nullable IndexablePage dbPage) {
        return PageIndexHelper.indexPageReplacements(page, dbPage);
    }
}
