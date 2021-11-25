package es.bvalero.replacer.page.index;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/** Index a page: calculate its replacements and update the related replacements in DB accordingly */
@Component
public class PageIndexer {

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
        if (!result.isEmpty()) {
            if (batchSave) {
                pageIndexResultSaver.saveBatch(result);
            } else {
                pageIndexResultSaver.save(result);
            }
        }
        // Return if the page has been processed, i.e. modifications have been applied in database.
        return !result.isEmpty();
    }

    /* Force saving what is left on the batch */
    public void forceSave() {
        pageIndexResultSaver.forceSave();
    }
}
