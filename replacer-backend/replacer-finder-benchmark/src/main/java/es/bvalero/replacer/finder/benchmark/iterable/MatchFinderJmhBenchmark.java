package es.bvalero.replacer.finder.benchmark.iterable;

import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.common.util.ReplacerUtils;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BaseFinderJmhBenchmark;
import es.bvalero.replacer.finder.util.FinderMatchResult;
import java.util.regex.MatchResult;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.RunnerException;
import org.springframework.lang.Nullable;

@Warmup(time = 5) // Default: 5 iterations, 10 s each
@Measurement(time = 5) // Default: 5 iterations, 10 s each
public class MatchFinderJmhBenchmark extends BaseFinderJmhBenchmark {

    private static final String fileName = "iterable/match-summary-jmh";
    private static final String word = "_";

    final MutableMatch match = new MutableMatch();

    @Override
    @Setup
    public void setUp() throws ReplacerException {
        // Base set-up
        super.setUp();
    }

    @Benchmark
    public void matchResultFinder(Blackhole bh) {
        sampleContents
            .stream()
            .flatMap(page -> ReplacerUtils.streamOfIterable(IterableMatchFinder.find(page, this::findMatch)))
            .forEach(bh::consume);
    }

    @Nullable
    private MatchResult findMatch(FinderPage page, int start) {
        final String text = page.getContent();
        if (start >= 0 && start < text.length()) {
            final int startMatch = findStartMatch(text, start);
            if (startMatch >= 0) {
                return FinderMatchResult.of(startMatch, word);
            } else {
                return null;
            }
        }
        return null;
    }

    @Benchmark
    public void matchRangeFinder(Blackhole bh) {
        sampleContents
            .stream()
            .flatMap(page -> ReplacerUtils.streamOfIterable(IterableMatchRangeFinder.find(page, this::findMatchRange)))
            .forEach(bh::consume);
    }

    @Nullable
    private MatchRange findMatchRange(FinderPage page, int start) {
        final String text = page.getContent();
        if (start >= 0 && start < text.length()) {
            final int startMatch = findStartMatch(text, start);
            if (startMatch >= 0) {
                return new MatchRange(startMatch, startMatch + word.length());
            } else {
                return null;
            }
        }
        return null;
    }

    @Benchmark
    public void matchRecordFinder(Blackhole bh) {
        sampleContents
            .stream()
            .flatMap(page -> ReplacerUtils.streamOfIterable(IterableMatchRecordFinder.find(page, this::findMatchRecord))
            )
            .forEach(bh::consume);
    }

    @Nullable
    private MatchRecord findMatchRecord(FinderPage page, int start) {
        final String text = page.getContent();
        if (start >= 0 && start < text.length()) {
            final int startMatch = findStartMatch(text, start);
            if (startMatch >= 0) {
                return new MatchRecord(startMatch, word);
            } else {
                return null;
            }
        }
        return null;
    }

    @Benchmark
    public void matchArrayFinder(Blackhole bh) {
        sampleContents
            .stream()
            .flatMap(page -> ReplacerUtils.streamOfIterable(IterableMatchArrayFinder.find(page, this::findMatchArray)))
            .forEach(bh::consume);
    }

    @Nullable
    private int[] findMatchArray(FinderPage page, int start) {
        final String text = page.getContent();
        if (start >= 0 && start < text.length()) {
            final int startMatch = findStartMatch(text, start);
            if (startMatch >= 0) {
                return new int[] { startMatch, startMatch + word.length() };
            } else {
                return null;
            }
        }
        return null;
    }

    @Benchmark
    public void mutableMatchFinder(Blackhole bh) {
        sampleContents
            .stream()
            .flatMap(page ->
                ReplacerUtils.streamOfIterable(IterableMutableMatchFinder.find(page, this::findMutableMatch, match))
            )
            .forEach(bh::consume);
    }

    private void findMutableMatch(FinderPage page, int start, MutableMatch match) {
        final String text = page.getContent();
        if (start >= 0 && start < text.length()) {
            match.setStart(findStartMatch(text, start));
        } else {
            match.setStart(-1);
        }
        match.setWord(word);
    }

    private static int findStartMatch(String text, int start) {
        return text.indexOf(word, start);
    }

    public static void main(String[] args) throws RunnerException, ReplacerException {
        run(MatchFinderJmhBenchmark.class, fileName);

        generateChart(fileName);
    }
}
