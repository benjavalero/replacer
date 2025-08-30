package es.bvalero.replacer.finder.benchmark.iterable;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.util.LinearFinder;
import java.util.Objects;
import java.util.regex.MatchResult;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StreamMatchFinder {

    public static Stream<MatchResult> find(FinderPage page, LinearFinder finder) {
        return Stream.iterate(finder.findResult(page, 0), Objects::nonNull, m -> finder.findResult(page, m.end()));
    }
}
