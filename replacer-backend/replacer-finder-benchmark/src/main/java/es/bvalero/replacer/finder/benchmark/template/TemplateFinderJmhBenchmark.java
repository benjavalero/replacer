package es.bvalero.replacer.finder.benchmark.template;

import es.bvalero.replacer.FinderProperties;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.finder.benchmark.BaseFinderJmhBenchmark;
import es.bvalero.replacer.finder.immutable.finders.TemplateFinder;
import es.bvalero.replacer.finder.immutable.finders.TemplateFinderOld;
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
public class TemplateFinderJmhBenchmark extends BaseFinderJmhBenchmark {

    private static final String fileName = "template/template-finder-summary-jmh";

    private ConfigurableApplicationContext context;

    private TemplateFinderOld templateFinderOld;
    private TemplateFinder templateFinder;

    @Override
    @Setup
    public void setUp() throws ReplacerException {
        super.setUp();

        context = SpringApplication.run(TemplateFinderJmhBenchmark.class);
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

        templateFinderOld = new TemplateFinderOld(finderProperties, uppercaseFinder);
        templateFinder = new TemplateFinder(finderProperties, uppercaseFinder);
        templateFinder.initTemplateParams();
    }

    @TearDown
    public void tearDown() {
        context.close();
    }

    @Benchmark
    public void templateFinderOld(Blackhole bh) {
        runFinder(templateFinderOld, bh);
    }

    @Benchmark
    public void templateFinder(Blackhole bh) {
        runFinder(templateFinder, bh);
    }

    public static void main(String[] args) throws RunnerException, ReplacerException {
        run(TemplateFinderJmhBenchmark.class, fileName);
        generateChart(fileName);
    }
}
