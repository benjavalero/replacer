package es.bvalero.replacer.finder.immutable.finders;

import es.bvalero.replacer.FinderProperties;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.finder.benchmark.BaseFinderJmhBenchmark;
import es.bvalero.replacer.finder.immutable.ImmutableFinder;
import es.bvalero.replacer.finder.listing.find.ListingFinder;
import es.bvalero.replacer.finder.listing.find.ListingOfflineFinder;
import es.bvalero.replacer.finder.listing.load.ComposedMisspellingLoader;
import es.bvalero.replacer.finder.listing.load.FalsePositiveLoader;
import es.bvalero.replacer.finder.listing.load.SimpleMisspellingLoader;
import es.bvalero.replacer.finder.listing.parse.ComposedMisspellingParser;
import es.bvalero.replacer.finder.listing.parse.FalsePositiveParser;
import es.bvalero.replacer.finder.listing.parse.SimpleMisspellingParser;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import lombok.SneakyThrows;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.RunnerException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;

@EnableConfigurationProperties(FinderProperties.class)
@Warmup(time = 10) // Default: 5 iterations, 10 s each
@Measurement(time = 10) // Default: 5 iterations, 10 s each
@State(Scope.Benchmark)
public class ImmutableFinderServiceJmhBenchmark extends BaseFinderJmhBenchmark {

    private static final String fileName = "../immutable/finders/immutable-service-summary-jmh";

    private ConfigurableApplicationContext context;

    private Collection<ImmutableFinder> finders;

    @Override
    @Setup
    public void setUp() throws ReplacerException {
        // Base set-up
        super.setUp();

        context = SpringApplication.run(ImmutableFinderServiceJmhBenchmark.class);
        context.registerShutdownHook();

        FinderProperties finderProperties = context.getBean(FinderProperties.class);

        CommentFinder commentFinder = new CommentFinder();
        CompleteTagFinder completeTagFinder = new CompleteTagFinder(finderProperties);
        completeTagFinder.init();
        CursiveFinder cursiveFinder = new CursiveFinder();
        ListingFinder listingOfflineFinder = new ListingOfflineFinder();
        FalsePositiveLoader falsePositiveLoader = new FalsePositiveLoader(
            listingOfflineFinder,
            new FalsePositiveParser()
        );
        FalsePositiveFinder falsePositiveFinder = new FalsePositiveFinder(falsePositiveLoader);
        falsePositiveFinder.init();
        IgnorableSectionFinder ignorableSectionFinder = new IgnorableSectionFinder(finderProperties);
        ignorableSectionFinder.init();
        RedirectionFinder redirectionFinder = new RedirectionFinder(finderProperties);
        redirectionFinder.init();
        PersonNameFinder personNameFinder = new PersonNameFinder(finderProperties);
        personNameFinder.init();
        PersonSurnameFinder personSurnameFinder = new PersonSurnameFinder(finderProperties);
        personSurnameFinder.init();
        QuotesAngularFinder quotesAngularFinder = new QuotesAngularFinder();
        QuotesDoubleFinder quotesDoubleFinder = new QuotesDoubleFinder();
        QuotesTypographicFinder quotesTypographicFinder = new QuotesTypographicFinder();
        // We need to initialize the uppercase finder first
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
        TemplateFinder templateFinder = new TemplateFinder(finderProperties, uppercaseFinder);
        templateFinder.initTemplateParams();
        LinkFinder linkFinder = new LinkFinder(finderProperties, uppercaseFinder);
        linkFinder.init();
        TitleFinder titleFinder = new TitleFinder();
        TableFinder tableFinder = new TableFinder();
        UrlFinder urlFinder = new UrlFinder();
        XmlTagFinder xmlTagFinder = new XmlTagFinder();

        // Load listings
        falsePositiveLoader.load();
        simpleMisspellingLoader.load();
        composedMisspellingLoader.load();

        finders = List.of(
            commentFinder,
            completeTagFinder,
            cursiveFinder,
            falsePositiveFinder,
            ignorableSectionFinder,
            redirectionFinder,
            personNameFinder,
            personSurnameFinder,
            quotesAngularFinder,
            quotesDoubleFinder,
            quotesTypographicFinder,
            uppercaseFinder,
            templateFinder,
            linkFinder,
            titleFinder,
            tableFinder,
            urlFinder,
            xmlTagFinder
        );
    }

    @TearDown
    public void tearDown() {
        context.close();
    }

    @Benchmark
    public void sequentialFinder(Blackhole bh) {
        sampleContents.forEach(page -> finders.stream().flatMap(finder -> finder.find(page)).forEach(bh::consume));
    }

    @Benchmark
    public void parallelFinder(Blackhole bh) {
        sampleContents.forEach(page ->
            finders.parallelStream().flatMap(finder -> finder.find(page)).forEach(bh::consume)
        );
    }

    @Benchmark
    public void parallelFinder1(Blackhole bh) {
        runParallelFinder(1, bh);
    }

    @Benchmark
    public void parallelFinder2(Blackhole bh) {
        runParallelFinder(2, bh);
    }

    @Benchmark
    public void parallelFinder4(Blackhole bh) {
        runParallelFinder(4, bh);
    }

    @Benchmark
    public void parallelFinder8(Blackhole bh) {
        runParallelFinder(8, bh);
    }

    public static void main(String[] args) throws RunnerException, ReplacerException {
        run(ImmutableFinderServiceJmhBenchmark.class, fileName);

        generateChart(fileName);
    }

    @SneakyThrows
    private void runParallelFinder(int threads, Blackhole bh) {
        try (ForkJoinPool customThreadPool = new ForkJoinPool(threads)) {
            customThreadPool
                .submit(() ->
                    sampleContents.forEach(page ->
                        finders.parallelStream().flatMap(finder -> finder.find(page)).forEach(bh::consume)
                    )
                )
                .get();
        }
    }
}
