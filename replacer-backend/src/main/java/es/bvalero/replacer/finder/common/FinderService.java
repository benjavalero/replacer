package es.bvalero.replacer.finder.common;

import es.bvalero.replacer.page.IndexablePage;
import java.util.List;
import java.util.stream.StreamSupport;
import org.apache.commons.collections4.IterableUtils;
import org.jetbrains.annotations.TestOnly;

public interface FinderService<T extends FinderResult> {
    // This method returns an Iterable in case we want to retrieve the results one-by-one,
    // for instance to improve performance.
    default Iterable<T> find(IndexablePage page) {
        // We include a default implementation that just creates an iterable
        // from all the results for each associated finder.
        return find(page, IterableUtils.toList(getFinders()));
    }

    default List<T> findList(IndexablePage page) {
        return IterableUtils.toList(find(page));
    }

    // Intermediate step to be able to pass dynamically the finders
    default Iterable<T> find(IndexablePage page, List<Finder<T>> finders) {
        return () ->
            finders.stream().flatMap(finder -> StreamSupport.stream(finder.find(page).spliterator(), false)).iterator();
    }

    Iterable<Finder<T>> getFinders();

    @TestOnly
    default List<T> findList(String text) {
        return IterableUtils.toList(this.find(FakePage.of(text)));
    }
}
