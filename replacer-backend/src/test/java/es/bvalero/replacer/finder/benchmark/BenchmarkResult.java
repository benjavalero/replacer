package es.bvalero.replacer.finder.benchmark;

import es.bvalero.replacer.common.domain.FinderResult;
import lombok.Value;

@Value(staticConstructor = "of")
public class BenchmarkResult implements FinderResult {

    int start;
    String text;
}
