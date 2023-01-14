package es.bvalero.replacer.finder;

import lombok.Value;

@Value(staticConstructor = "of")
public class BenchmarkResult implements FinderResult {

    int start;
    String text;
}
