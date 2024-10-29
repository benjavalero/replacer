package es.bvalero.replacer.finder.replacement.finders;

import es.bvalero.replacer.FinderProperties;
import es.bvalero.replacer.finder.benchmark.BaseFinderJmhBenchmark;
import es.bvalero.replacer.finder.listing.find.ListingFinder;
import es.bvalero.replacer.finder.listing.find.ListingOfflineFinder;
import es.bvalero.replacer.finder.listing.load.ComposedMisspellingLoader;
import es.bvalero.replacer.finder.listing.load.SimpleMisspellingLoader;
import es.bvalero.replacer.finder.listing.parse.ComposedMisspellingParser;
import es.bvalero.replacer.finder.listing.parse.SimpleMisspellingParser;
import es.bvalero.replacer.wikipedia.WikipediaException;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.RunnerException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;

@EnableConfigurationProperties(FinderProperties.class)
@State(Scope.Benchmark)
public class ReplacementFinderJmhBenchmarkTest extends BaseFinderJmhBenchmark {

    private static final String fileName = "../replacement/finders/replacement-summary-jmh";

    private ConfigurableApplicationContext context;

    private AcuteOFinder acuteOFinder;
    private CenturyFinder centuryFinder;
    private CoordinatesFinder coordinatesFinder;
    private DateFinder dateFinder;
    private DegreeFinder degreeFinder;
    private OrdinalFinder ordinalFinder;
    private MisspellingComposedFinder misspellingComposedFinder;
    private MisspellingSimpleFinder misspellingSimpleFinder;

    @Setup
    public void setUp() throws WikipediaException {
        // Base set-up
        super.setUp();

        context = SpringApplication.run(ReplacementFinderJmhBenchmarkTest.class);
        context.registerShutdownHook();

        FinderProperties finderProperties = context.getBean(FinderProperties.class);

        acuteOFinder = new AcuteOFinder();
        centuryFinder = new CenturyFinder();
        coordinatesFinder = new CoordinatesFinder();
        dateFinder = new DateFinder(finderProperties);
        dateFinder.init();
        degreeFinder = new DegreeFinder();
        ordinalFinder = new OrdinalFinder(finderProperties);
        ordinalFinder.init();
        ListingFinder listingOfflineFinder = new ListingOfflineFinder();
        ComposedMisspellingLoader composedMisspellingLoader = new ComposedMisspellingLoader(
            listingOfflineFinder,
            new ComposedMisspellingParser()
        );
        misspellingComposedFinder = new MisspellingComposedFinder(composedMisspellingLoader);
        misspellingComposedFinder.init();
        SimpleMisspellingLoader simpleMisspellingLoader = new SimpleMisspellingLoader(
            listingOfflineFinder,
            new SimpleMisspellingParser()
        );
        misspellingSimpleFinder = new MisspellingSimpleFinder(simpleMisspellingLoader);
        misspellingSimpleFinder.init();

        // Load listings
        simpleMisspellingLoader.load();
        composedMisspellingLoader.load();
    }

    @TearDown
    public void tearDown() {
        context.close();
    }

    @Benchmark
    public void acuteOFinder(Blackhole bh) {
        runFinder(acuteOFinder, bh);
    }

    @Benchmark
    public void centuryFinder(Blackhole bh) {
        runFinder(centuryFinder, bh);
    }

    @Benchmark
    public void coordinatesFinder(Blackhole bh) {
        runFinder(coordinatesFinder, bh);
    }

    @Benchmark
    public void dateFinder(Blackhole bh) {
        runFinder(dateFinder, bh);
    }

    @Benchmark
    public void degreeFinder(Blackhole bh) {
        runFinder(degreeFinder, bh);
    }

    @Benchmark
    public void ordinalFinder(Blackhole bh) {
        runFinder(ordinalFinder, bh);
    }

    @Benchmark
    public void misspellingComposedFinder(Blackhole bh) {
        runFinder(misspellingComposedFinder, bh);
    }

    @Benchmark
    public void misspellingSimpleFinder(Blackhole bh) {
        runFinder(misspellingSimpleFinder, bh);
    }

    public static void main(String[] args) throws RunnerException {
        run(ReplacementFinderJmhBenchmarkTest.class, fileName);
    }
}
