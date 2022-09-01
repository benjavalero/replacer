package es.bvalero.replacer.finder.benchmark;

import static tech.tablesaw.aggregate.AggregateFunctions.*;
import static tech.tablesaw.aggregate.AggregateFunctions.max;

import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.api.WikipediaUtils;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Objects;
import tech.tablesaw.api.Table;
import tech.tablesaw.columns.numbers.NumberColumnFormatter;
import tech.tablesaw.io.csv.CsvReadOptions;
import tech.tablesaw.plotly.Plot;
import tech.tablesaw.plotly.api.BoxPlot;
import tech.tablesaw.plotly.components.Figure;

public abstract class BaseFinderBenchmark {

    public static final int WARM_UP = 100;
    public static final int ITERATIONS = 1000;

    protected void runBenchmark(List<BenchmarkFinder> finders) throws ReplacerException {
        runBenchmark(finders, WARM_UP, ITERATIONS);
    }

    protected void runBenchmark(List<BenchmarkFinder> finders, int warmUp, int iterations) throws ReplacerException {
        try {
            List<WikipediaPage> sampleContents = WikipediaUtils.findSampleContents();

            // Warm-up
            System.out.println("WARM-UP...");
            run(finders, warmUp, sampleContents, false);

            // Real run
            run(finders, iterations, sampleContents, true);
        } catch (WikipediaException e) {
            throw new ReplacerException(e);
        }
    }

    private void run(
        List<BenchmarkFinder> finders,
        int numIterations,
        List<WikipediaPage> sampleContents,
        boolean print
    ) {
        if (print) {
            System.out.println();
            System.out.println("FINDER\tTIME");
        }
        sampleContents.forEach(page -> {
            for (BenchmarkFinder finder : finders) {
                long start = System.nanoTime();
                for (int i = 0; i < numIterations; i++) {
                    finder.findMatches(page);
                }
                double end = (double) (System.nanoTime() - start) / 1000.0; // In µs
                if (print) {
                    System.out.println(finder.getClass().getSimpleName() + "\t" + end);
                }
            }
        });
    }

    protected List<WikipediaPage> findSampleContents() throws ReplacerException {
        try {
            return WikipediaUtils.findSampleContents();
        } catch (WikipediaException e) {
            throw new ReplacerException(e);
        }
    }

    protected static void generateBoxplot(String fileName, String title) throws URISyntaxException {
        // Generate boxplot
        final Path csvPath = Paths.get(Objects.requireNonNull(BaseFinderBenchmark.class.getResource(fileName)).toURI());
        CsvReadOptions options = CsvReadOptions.builder(csvPath.toFile()).separator('\t').build();
        Table table = Table.read().usingOptions(options);

        Table summary = table.summarize("TIME", median, min, mean, max).by("FINDER");
        DecimalFormat df = new DecimalFormat("0.000");
        NumberColumnFormatter ncf = new NumberColumnFormatter(df);
        summary.doubleColumn(1).setPrintFormatter(ncf);
        summary.doubleColumn(2).setPrintFormatter(ncf);
        summary.doubleColumn(3).setPrintFormatter(ncf);
        summary.doubleColumn(4).setPrintFormatter(ncf);
        System.out.println(summary);

        Figure boxplot = BoxPlot.create(title, table, "FINDER", "TIME");
        Plot.show(boxplot);
    }
}
