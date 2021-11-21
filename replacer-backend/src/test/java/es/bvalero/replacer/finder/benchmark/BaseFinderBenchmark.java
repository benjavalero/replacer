package es.bvalero.replacer.finder.benchmark;

import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.wikipedia.api.WikipediaUtils;
import java.util.List;

public abstract class BaseFinderBenchmark {

    public static final int WARM_UP = 100;
    public static final int ITERATIONS = 1000;

    protected void runBenchmark(List<BenchmarkFinder> finders) throws ReplacerException {
        runBenchmark(finders, WARM_UP, ITERATIONS);
    }

    protected void runBenchmark(List<BenchmarkFinder> finders, int warmUp, int iterations) throws ReplacerException {
        List<String> sampleContents = WikipediaUtils.findSampleContents();

        // Warm-up
        System.out.println("WARM-UP...");
        run(finders, warmUp, sampleContents, false);

        // Real run
        run(finders, iterations, sampleContents, true);
    }

    private void run(List<BenchmarkFinder> finders, int numIterations, List<String> sampleContents, boolean print) {
        if (print) {
            System.out.println();
            System.out.println("FINDER\tTIME");
        }
        sampleContents.forEach(
            text -> {
                for (BenchmarkFinder finder : finders) {
                    long start = System.nanoTime();
                    for (int i = 0; i < numIterations; i++) {
                        finder.findMatches(text);
                    }
                    double end = (double) (System.nanoTime() - start) / 1000.0; // In µs
                    if (print) {
                        System.out.println(finder.getClass().getSimpleName() + "\t" + end);
                    }
                }
            }
        );
    }

    protected List<String> findSampleContents() throws ReplacerException {
        return WikipediaUtils.findSampleContents();
    }
}
