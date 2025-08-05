package es.bvalero.replacer.finder.benchmark.page;

import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.common.domain.PageKey;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.finder.benchmark.BaseFinderJmhBenchmark;
import es.bvalero.replacer.finder.benchmark.util.BenchmarkUtils;
import es.bvalero.replacer.finder.benchmark.util.WikipediaApiResponse;
import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.Value;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.RunnerException;

@Warmup(time = 5)
@Measurement(time = 5)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class NamespaceJmhBenchmarkTest extends BaseFinderJmhBenchmark {

    private static final String fileName = "page/namespace-summary-jmh";

    private List<WikipediaApiResponse.Page> samplePages;

    @Override
    @Setup
    public void setUp() throws ReplacerException {
        samplePages = BenchmarkUtils.findSamplePages();
    }

    @Benchmark
    public void enumConverter(Blackhole bh) {
        samplePages.stream().map(this::convertEnumPage).forEach(bh::consume);
    }

    private FinderEnumPage convertEnumPage(WikipediaApiResponse.Page page) {
        return FinderEnumPage.of(
            PageKey.of(WikipediaLanguage.getDefault(), page.getPageid()),
            WikipediaNamespace.valueOf(page.getNs()),
            page.getTitle(),
            page.getRevisions().stream().findFirst().orElseThrow().getSlots().getMain().getContent()
        );
    }

    @Benchmark
    public void intConverter(Blackhole bh) {
        samplePages.stream().map(this::convertIntPage).forEach(bh::consume);
    }

    private FinderIntPage convertIntPage(WikipediaApiResponse.Page page) {
        return FinderIntPage.of(
            PageKey.of(WikipediaLanguage.getDefault(), page.getPageid()),
            page.getNs(),
            page.getTitle(),
            page.getRevisions().stream().findFirst().orElseThrow().getSlots().getMain().getContent()
        );
    }

    @Test
    void testGenerateChartBoxplot() throws ReplacerException {
        generateChart(fileName);
        assertTrue(true);
    }

    public static void main(String[] args) throws RunnerException {
        run(NamespaceJmhBenchmarkTest.class, fileName);
    }

    @Value(staticConstructor = "of")
    private static class FinderEnumPage {

        PageKey pageKey;
        WikipediaNamespace ns;
        String title;
        String content;
    }

    @Value(staticConstructor = "of")
    private static class FinderIntPage {

        PageKey pageKey;
        int ns;
        String title;
        String content;
    }
}
