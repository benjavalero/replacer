package es.bvalero.replacer.finder.benchmark;

import static tech.tablesaw.aggregate.AggregateFunctions.*;
import static tech.tablesaw.aggregate.AggregateFunctions.max;

import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.finder.Finder;
import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.api.WikipediaUtils;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Objects;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.lang.Nullable;
import tech.tablesaw.api.Table;
import tech.tablesaw.columns.numbers.NumberColumnFormatter;
import tech.tablesaw.io.csv.CsvReadOptions;
import tech.tablesaw.plotly.Plot;
import tech.tablesaw.plotly.api.BoxPlot;
import tech.tablesaw.plotly.components.Figure;

public abstract class BaseFinderBenchmark {

    public static final int WARM_UP = 100;
    public static final int ITERATIONS = 1000;

    protected void runBenchmark(List<Finder<?>> finders, String fileName) throws ReplacerException {
        runBenchmark(finders, WARM_UP, ITERATIONS, fileName);
    }

    protected void runBenchmark(List<Finder<?>> finders, int warmUp, int iterations, String fileName)
        throws ReplacerException {
        try {
            List<WikipediaPage> sampleContents = WikipediaUtils.findSampleContents();

            // Warm-up
            run(finders, warmUp, sampleContents, null);

            // Real run
            String testResourcesPath = "src/test/resources/es/bvalero/replacer/finder/benchmark/";
            File csvFile = new File(testResourcesPath + fileName);
            BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile));
            run(finders, iterations, sampleContents, writer);
            writer.close();
        } catch (WikipediaException | IOException e) {
            throw new ReplacerException(e);
        }
    }

    private void run(
        List<Finder<?>> finders,
        int numIterations,
        List<WikipediaPage> sampleContents,
        @Nullable BufferedWriter writer
    ) {
        boolean print = (writer != null);
        if (print) {
            String headers = "FINDER\tTIME\n";
            print(writer, headers);
            System.out.print(headers);
        }
        sampleContents.forEach(page -> {
            for (Finder<?> finder : finders) {
                long start = System.nanoTime();
                for (int i = 0; i < numIterations; i++) {
                    // Only transform the iterable without validating the positions not to penalize the performance of the benchmark
                    IterableUtils.toList(finder.find(page));
                }
                double end = (double) (System.nanoTime() - start) / 1000.0; // In µs
                if (print) {
                    String time = finder.getClass().getSimpleName() + '\t' + end + '\n';
                    print(writer, time);
                    System.out.print(time);
                }
            }
        });
    }

    private void print(BufferedWriter writer, String str) {
        try {
            writer.append(str);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected static void generateBoxplot(String fileName, String title) throws URISyntaxException, IOException {
        // Generate boxplot
        final Path csvPath = Paths.get(Objects.requireNonNull(BaseFinderBenchmark.class.getResource(fileName)).toURI());
        CsvReadOptions options = CsvReadOptions.builder(csvPath.toFile()).separator('\t').build();
        Table table = Table.read().usingOptions(options);

        Table summary = table.summarize("TIME", mean, min, quartile1, median, quartile3, max).by("FINDER");
        DecimalFormat df = new DecimalFormat("0.000");
        NumberColumnFormatter ncf = new NumberColumnFormatter(df);
        summary.doubleColumn(1).setPrintFormatter(ncf);
        summary.doubleColumn(2).setPrintFormatter(ncf);
        summary.doubleColumn(3).setPrintFormatter(ncf);
        summary.doubleColumn(4).setPrintFormatter(ncf);
        summary.doubleColumn(5).setPrintFormatter(ncf);
        summary.doubleColumn(6).setPrintFormatter(ncf);

        // Print summary
        System.out.println(summary);

        String testResourcesPath = "src/test/resources/es/bvalero/replacer/finder/benchmark/";
        String txtName = fileName.replace("benchmark.csv", "summary.txt");
        File txtFile = new File(testResourcesPath + txtName);
        BufferedWriter writer = new BufferedWriter(new FileWriter(txtFile));
        writer.write(summary.toString());
        writer.close();

        Figure boxplot = BoxPlot.create(title, table, "FINDER", "TIME");
        Plot.show(boxplot);
    }
}
