package es.bvalero.replacer.finder.benchmark.link;

import es.bvalero.replacer.FinderProperties;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.finder.benchmark.BaseFinderJmhBenchmark;
import es.bvalero.replacer.finder.immutable.finders.LinkFinder;
import es.bvalero.replacer.finder.immutable.finders.LinkFinderOld;
import es.bvalero.replacer.finder.immutable.finders.UppercaseFinder;
import es.bvalero.replacer.finder.listing.find.ListingOfflineFinder;
import es.bvalero.replacer.finder.listing.load.ComposedMisspellingLoader;
import es.bvalero.replacer.finder.listing.load.SimpleMisspellingLoader;
import es.bvalero.replacer.finder.listing.parse.ComposedMisspellingParser;
import es.bvalero.replacer.finder.listing.parse.SimpleMisspellingParser;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.RunnerException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;

@EnableConfigurationProperties(FinderProperties.class)
@State(Scope.Benchmark)
public class LinkFinderJmhBenchmark extends BaseFinderJmhBenchmark {

    private static final String fileName = "link/link-finder-summary-jmh";

    private ConfigurableApplicationContext context;

    private LinkFinderOld linkFinderOld;
    private LinkFinder linkFinder;

    @Override
    @Setup
    public void setUp() throws ReplacerException {
        super.setUp();

        context = SpringApplication.run(LinkFinderJmhBenchmark.class);
        context.registerShutdownHook();

        FinderProperties finderProperties = context.getBean(FinderProperties.class);

        ListingOfflineFinder listingOfflineFinder = new ListingOfflineFinder();
        SimpleMisspellingLoader simpleMisspellingLoader = new SimpleMisspellingLoader(
            listingOfflineFinder,
            new SimpleMisspellingParser()
        );
        ComposedMisspellingLoader composedMisspellingLoader = new ComposedMisspellingLoader(
            listingOfflineFinder,
            new ComposedMisspellingParser()
        );
        UppercaseFinder uppercaseFinder = new UppercaseFinder(simpleMisspellingLoader, composedMisspellingLoader);
        uppercaseFinder.init();
        simpleMisspellingLoader.load();
        composedMisspellingLoader.load();

        linkFinderOld = new LinkFinderOld(finderProperties, uppercaseFinder);
        linkFinder = new LinkFinder(finderProperties, uppercaseFinder);
        linkFinder.init();
    }

    @TearDown
    public void tearDown() {
        context.close();
    }

    @Benchmark
    public void linkFinderOld(Blackhole bh) {
        runFinder(linkFinderOld, bh);
    }

    @Benchmark
    public void linkFinder(Blackhole bh) {
        runFinder(linkFinder, bh);
    }

    public static void main(String[] args) throws RunnerException, ReplacerException {
        run(LinkFinderJmhBenchmark.class, fileName);
        generateChart(fileName);
    }
}
