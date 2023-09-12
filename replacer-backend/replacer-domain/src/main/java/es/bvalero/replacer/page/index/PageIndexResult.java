package es.bvalero.replacer.page.index;

import es.bvalero.replacer.finder.Replacement;
import java.util.Collection;
import java.util.Collections;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Value;
import org.jetbrains.annotations.TestOnly;
import org.springframework.lang.NonNull;

/** Result of indexing a page or several pages */
@Value(staticConstructor = "of")
@Builder(access = AccessLevel.PRIVATE)
public class PageIndexResult {

    /* Resulting status of the page indexing */

    @NonNull
    @Builder.Default
    PageIndexStatus status = PageIndexStatus.PAGE_NOT_INDEXED;

    /* Replacements resolved from the page content */

    @NonNull
    @Builder.Default
    Collection<Replacement> replacements = Collections.emptyList();

    public static PageIndexResult ofNotIndexable() {
        return PageIndexResult.builder().status(PageIndexStatus.PAGE_NOT_INDEXABLE).build();
    }

    public static PageIndexResult ofNotIndexed() {
        return PageIndexResult.builder().status(PageIndexStatus.PAGE_NOT_INDEXED).build();
    }

    @TestOnly
    public static PageIndexResult ofIndexed() {
        // This is only for tests because being indexed doesn't imply having replacements to review and vice versa
        return PageIndexResult.builder().status(PageIndexStatus.PAGE_INDEXED).build();
    }

    @TestOnly
    public static PageIndexResult ofIndexed(Collection<Replacement> replacements) {
        // This is only for tests because being indexed doesn't imply having replacements to review and vice versa
        return PageIndexResult.of(PageIndexStatus.PAGE_INDEXED, replacements);
    }

    public static PageIndexResult of(PageIndexStatus status, Collection<Replacement> replacements) {
        return PageIndexResult.builder().status(status).replacements(replacements).build();
    }
}
