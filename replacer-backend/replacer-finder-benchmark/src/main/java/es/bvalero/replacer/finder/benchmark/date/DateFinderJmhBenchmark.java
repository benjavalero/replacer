package es.bvalero.replacer.finder.benchmark.date;

import es.bvalero.replacer.FinderProperties;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.finder.benchmark.BaseFinderJmhBenchmark;
import es.bvalero.replacer.finder.replacement.finders.DateFinder;
import lombok.SneakyThrows;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * JMH Benchmark comparing DateFinderOld (automaton baseline)
 * vs DateFinder (current production version being optimized).
 */
@EnableConfigurationProperties(FinderProperties.class)
@State(Scope.Benchmark)
public class DateFinderJmhBenchmark extends BaseFinderJmhBenchmark {

    private static final String fileName = "date/date-summary-jmh";

    private ConfigurableApplicationContext context;

    private DateFinderOld dateFinderOld;
    private DateFinder dateFinder;

    @Override
    @Setup
    public void setUp() throws ReplacerException {
        super.setUp();

        context = SpringApplication.run(DateFinderJmhBenchmark.class);
        context.registerShutdownHook();

        FinderProperties finderProperties = context.getBean(FinderProperties.class);

        dateFinderOld = new DateFinderOld(finderProperties);
        dateFinderOld.init();

        dateFinder = new DateFinder(finderProperties);
        dateFinder.init();
    }

    @TearDown
    public void tearDown() {
        context.close();
    }

    @Benchmark
    public void dateFinderOld(Blackhole bh) {
        runReplacementFinder(dateFinderOld, bh);
    }

    @Benchmark
    public void dateFinder(Blackhole bh) {
        runReplacementFinder(dateFinder, bh);
    }

    @SneakyThrows
    public static void main(String[] args) {
        run(DateFinderJmhBenchmark.class, fileName);
        generateChart(fileName);
    }
}
