package es.bvalero.replacer.finder.benchmark;

import es.bvalero.replacer.finder.FinderResult;

public record BenchmarkResult(int start, String text) implements FinderResult {
    public static BenchmarkResult of(int start, String text) {
        return new BenchmarkResult(start, text);
    }
}
