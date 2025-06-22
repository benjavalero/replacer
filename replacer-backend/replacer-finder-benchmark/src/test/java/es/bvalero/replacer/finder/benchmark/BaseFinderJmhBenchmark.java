package es.bvalero.replacer.finder.benchmark;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.finder.Finder;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.util.BenchmarkUtils;
import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.StatisticalBarRenderer;
import org.jfree.data.statistics.DefaultStatisticalCategoryDataset;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@Threads(1) // Default
@Fork(value = 1, jvmArgsAppend = { "-Xmx256m", "-da" }) // 0 makes debugging possible, disable assertions just in case
@State(Scope.Benchmark)
@Warmup(time = 2) // Default: 5 iterations, 10 s each
@Measurement(time = 2) // Default: 5 iterations, 10 s each
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class BaseFinderJmhBenchmark {

    public static final String BENCHMARK_PACKAGE_PATH = "/es/bvalero/replacer/finder/benchmark/";
    public static final String TEST_RESOURCES_PATH = "src/test/resources" + BENCHMARK_PACKAGE_PATH;
    private static final String TEST_RESOURCES_COMPLETE_PATH =
        "replacer-backend/replacer-finder-benchmark/" + TEST_RESOURCES_PATH;

    protected List<FinderPage> sampleContents;
    private Map<Integer, Integer> samplePages;

    // @Param({"1", "2", "3"})
    public int pageId = 0;

    // Note: Timeout is 10 min per iteration by default.
    // However, it only works on the teardown phase,
    // so it is not useful to interrupt very long benchmarks.

    protected void setUp() throws ReplacerException {
        // The pages have been sampled so the distribution of their content lengths
        // matches the one of the content lengths of all indexable pages
        sampleContents = BenchmarkUtils.findSampleContents();

        // We choose 3 sample pages to represent different sizes
        // 1: 8597062   // Small (2,5 kB)
        // 2: 811007    // Medium (6 kB)
        // 3: 7707926   // Large (14 kB)
        samplePages = Map.of(1, 8597062, 2, 811007, 3, 7707926);
    }

    protected void runFinder(Finder<?> finder, Blackhole bh) {
        if (pageId == 0) {
            // As the sample represents the whole dump,
            // finding all over the sample represents the time finding all over the dump.
            // NOTE: therefore, the average time corresponds to run the finder in the 50 sample pages.
            sampleContents.forEach(page -> finder.find(page).forEach(bh::consume));
        } else {
            sampleContents
                .stream()
                .filter(p -> p.getPageKey().getPageId() == samplePages.get(pageId))
                .forEach(page -> finder.find(page).forEach(bh::consume));
        }
    }

    @Benchmark
    public void baseLine() {
        // Do nothing
    }

    protected static void run(Class<? extends BaseFinderJmhBenchmark> jmhBenchmarkClass, String fileName)
        throws RunnerException {
        Options opt = new OptionsBuilder()
            .include(jmhBenchmarkClass.getSimpleName())
            // .addProfiler(GCProfiler.class)
            // .addProfiler(MemPoolProfiler.class)
            .resultFormat(ResultFormatType.JSON)
            .result(TEST_RESOURCES_COMPLETE_PATH + fileName + ".json")
            .build();

        new Runner(opt).run();
    }

    protected static void generateChart(String fileName) throws ReplacerException {
        DefaultStatisticalCategoryDataset dataset = buildDataset(getJsonSummaries(getJsonFile(fileName)));
        JFreeChart chart = ChartFactory.createBarChart(null, null, "Execution Time (µs)", dataset);

        // Customize the plot to use StatisticalBarRenderer for error bars
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        StatisticalBarRenderer renderer = new StatisticalBarRenderer();
        renderer.setDrawBarOutline(true);
        renderer.setErrorIndicatorPaint(Color.BLACK);
        plot.setRenderer(renderer);

        // Save the chart as a PNG image in the same folder than the JSON
        // Note: from the same dataset the image can be regenerated with no difference
        saveChart(chart, fileName);
    }

    private static File getJsonFile(String fileName) throws ReplacerException {
        try {
            return Paths.get(
                Objects.requireNonNull(
                    BaseFinderJmhBenchmark.class.getResource(BENCHMARK_PACKAGE_PATH + fileName + ".json")
                ).toURI()
            ).toFile();
        } catch (URISyntaxException e) {
            throw new ReplacerException(e);
        }
    }

    private static JmhBenchmarkSummary[] getJsonSummaries(File jsonFile) throws ReplacerException {
        try {
            String jsonContent = getFileContent(jsonFile);
            ObjectMapper jsonMapper = new ObjectMapper();
            return jsonMapper.readValue(jsonContent, JmhBenchmarkSummary[].class);
        } catch (Exception e) {
            throw new ReplacerException(e);
        }
    }

    private static String getFileContent(File file) throws ReplacerException {
        try (InputStream is = new FileInputStream(file)) {
            return new String(Objects.requireNonNull(is).readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ReplacerException(e);
        }
    }

    private static DefaultStatisticalCategoryDataset buildDataset(JmhBenchmarkSummary[] summaries) {
        DefaultStatisticalCategoryDataset dataset = new DefaultStatisticalCategoryDataset();
        for (JmhBenchmarkSummary summary : summaries) {
            String benchmark = summary.getBenchmark();
            String benchmarkName = benchmark.substring(benchmark.lastIndexOf('.') + 1);
            double score = Double.parseDouble(summary.getPrimaryMetric().getScore());
            double scoreError = Double.parseDouble(summary.getPrimaryMetric().getScoreError());
            if (!benchmarkName.equals("baseLine")) {
                dataset.add(score, scoreError, benchmarkName, "");
            }
        }
        return dataset;
    }

    private static void saveChart(JFreeChart chart, String fileName) throws ReplacerException {
        try {
            int posSeparator = fileName.lastIndexOf('/');
            String folder, name;
            if (posSeparator >= 0) {
                folder = fileName.substring(0, posSeparator);
                name = fileName.substring(posSeparator + 1);
            } else {
                folder = "";
                name = fileName;
            }
            File chartFile = new File(TEST_RESOURCES_PATH + folder, name + ".png");
            ChartUtils.saveChartAsPNG(chartFile, chart, 800, 600);
        } catch (IOException e) {
            throw new ReplacerException(e);
        }
    }
}
