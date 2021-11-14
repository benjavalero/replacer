package es.bvalero.replacer.page.index;

import es.bvalero.replacer.page.repository.IndexablePageDB;
import es.bvalero.replacer.page.repository.IndexableReplacementDB;
import java.util.List;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

/** Sub-domain object representing the result of indexing a page or several pages */
@Value
@Builder(toBuilder = true)
class PageIndexResult {

    // Pages to be created along with the related replacements
    @Singular
    List<IndexablePageDB> createPages;

    // The attributes of a page may vary with time. In particular, we are interested in update the titles.
    @Singular
    List<IndexablePageDB> updatePages;

    @Singular
    List<IndexableReplacementDB> createReplacements;

    // The attributes of a replacement may vary, e.g. the last update date, or the context/position.
    @Singular
    List<IndexableReplacementDB> updateReplacements;

    @Singular
    List<IndexableReplacementDB> deleteReplacements;

    int size() {
        return (
            createPages.size() +
            updatePages.size() +
            createReplacements.size() +
            updateReplacements.size() +
            deleteReplacements.size()
        );
    }
}
