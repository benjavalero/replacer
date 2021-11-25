package es.bvalero.replacer.page.index;

import es.bvalero.replacer.common.domain.WikipediaPageId;
import es.bvalero.replacer.page.repository.PageRepository;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/** Index a page: calculate its replacements and update the related replacements in DB accordingly */
@Component
public class PageIndexer {

    @Autowired
    private PageRepository pageRepository;

    @Autowired
    private PageIndexResultSaver pageIndexResultSaver;

    public void indexPageReplacements(@Nullable IndexablePage page, @Nullable IndexablePage dbPage) {
        indexPageReplacements(page, dbPage, false);
    }

    public boolean indexPageReplacements(
        @Nullable IndexablePage page,
        @Nullable IndexablePage dbPage,
        boolean batchSave
    ) {
        PageIndexResult result = PageIndexHelper.indexPageReplacements(page, dbPage);
        saveResult(result, batchSave);

        // Return if the page has been processed, i.e. modifications have been applied in database.
        return !result.isEmpty();
    }

    private void saveResult(PageIndexResult result, boolean batchSave) {
        if (!result.isEmpty()) {
            if (batchSave) {
                pageIndexResultSaver.saveBatch(result);
            } else {
                pageIndexResultSaver.save(result);
            }
        }
    }

    /** Index a page which should not be in database because it has been deleted or is not processable anymore */
    public void indexObsoletePage(IndexablePage dbPage, boolean batchSave) {
        saveResult(PageIndexResult.builder().deletePages(Set.of(dbPage)).build(), batchSave);
    }

    /** Index a page which should not be in database because it has been deleted or is not processable anymore */
    public void indexObsoletePage(WikipediaPageId pageId) {
        pageRepository
            .findByPageId(pageId)
            .map(IndexablePageMapper::fromModel)
            .ifPresent(page -> indexObsoletePage(page, false));
    }

    /* Force saving what is left on the batch */
    public void forceSave() {
        pageIndexResultSaver.forceSave();
    }
}
