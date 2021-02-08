package es.bvalero.replacer.finder.benchmark;

import es.bvalero.replacer.finder.common.FinderResult;
import lombok.Value;

@Value(staticConstructor = "of")
public class BenchmarkResult implements FinderResult {

    int start;
    String text;
}
