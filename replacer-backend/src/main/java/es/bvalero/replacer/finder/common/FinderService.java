package es.bvalero.replacer.finder.common;

import com.google.common.collect.Iterables;
import es.bvalero.replacer.page.IndexablePage;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections4.IterableUtils;
import org.jetbrains.annotations.TestOnly;

public interface FinderService<T extends FinderResult> {
    // This method returns an Iterable in case we want to retrieve the results one-by-one,
    // for instance to improve performance.
    default Iterable<T> find(IndexablePage page) {
        // We include a default implementation that just creates an iterable
        // from all the results for each associated finder.
        Iterable<Iterable<T>> iterableList =
            this.getFinders().stream().map(finder -> finder.find(page)).collect(Collectors.toUnmodifiableList());
        return Iterables.concat(iterableList);
    }

    List<Finder<T>> getFinders();

    @TestOnly
    default List<T> findList(String text) {
        return IterableUtils.toList(this.find(FakePage.of(text)));
    }
}
