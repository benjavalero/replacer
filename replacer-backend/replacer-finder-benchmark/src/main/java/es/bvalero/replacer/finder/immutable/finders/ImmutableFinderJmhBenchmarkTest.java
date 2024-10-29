package es.bvalero.replacer.finder.immutable.finders;

import es.bvalero.replacer.FinderProperties;
import es.bvalero.replacer.finder.benchmark.BaseFinderJmhBenchmark;
import es.bvalero.replacer.finder.listing.find.ListingFinder;
import es.bvalero.replacer.finder.listing.find.ListingOfflineFinder;
import es.bvalero.replacer.finder.listing.load.ComposedMisspellingLoader;
import es.bvalero.replacer.finder.listing.load.FalsePositiveLoader;
import es.bvalero.replacer.finder.listing.load.SimpleMisspellingLoader;
import es.bvalero.replacer.finder.listing.parse.ComposedMisspellingParser;
import es.bvalero.replacer.finder.listing.parse.FalsePositiveParser;
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
public class ImmutableFinderJmhBenchmarkTest extends BaseFinderJmhBenchmark {

    private static final String fileName = "../immutable/finders/immutable-summary-jmh";

    private ConfigurableApplicationContext context;

    private CommentFinder commentFinder;
    private CompleteTagFinder completeTagFinder;
    private CursiveFinder cursiveFinder;
    private FalsePositiveFinder falsePositiveFinder;
    private IgnorableSectionFinder ignorableSectionFinder;
    private RedirectionFinder redirectionFinder;
    private LinkFinder linkFinder;
    private PersonNameFinder personNameFinder;
    private PersonSurnameFinder personSurnameFinder;
    private QuotesAngularFinder quotesAngularFinder;
    private QuotesDoubleFinder quotesDoubleFinder;
    private QuotesTypographicFinder quotesTypographicFinder;
    private TemplateFinder templateFinder;
    private TitleFinder titleFinder;
    private TableFinder tableFinder;
    private UppercaseFinder uppercaseFinder;
    private UrlFinder urlFinder;
    private XmlTagFinder xmlTagFinder;

    @Setup
    public void setUp() throws WikipediaException {
        // Base set-up
        super.setUp();

        context = SpringApplication.run(ImmutableFinderJmhBenchmarkTest.class);
        context.registerShutdownHook();

        FinderProperties finderProperties = context.getBean(FinderProperties.class);

        commentFinder = new CommentFinder();
        completeTagFinder = new CompleteTagFinder(finderProperties);
        completeTagFinder.init();
        cursiveFinder = new CursiveFinder();
        ListingFinder listingOfflineFinder = new ListingOfflineFinder();
        FalsePositiveLoader falsePositiveLoader = new FalsePositiveLoader(
            listingOfflineFinder,
            new FalsePositiveParser()
        );
        falsePositiveFinder = new FalsePositiveFinder(falsePositiveLoader);
        falsePositiveFinder.init();
        ignorableSectionFinder = new IgnorableSectionFinder(finderProperties);
        ignorableSectionFinder.init();
        redirectionFinder = new RedirectionFinder(finderProperties);
        redirectionFinder.init();
        linkFinder = new LinkFinder(finderProperties);
        linkFinder.init();
        personNameFinder = new PersonNameFinder(finderProperties);
        personNameFinder.init();
        personSurnameFinder = new PersonSurnameFinder(finderProperties);
        personSurnameFinder.init();
        quotesAngularFinder = new QuotesAngularFinder();
        quotesDoubleFinder = new QuotesDoubleFinder();
        quotesTypographicFinder = new QuotesTypographicFinder();
        // We need to initialize the uppercase finder first
        SimpleMisspellingLoader simpleMisspellingLoader = new SimpleMisspellingLoader(
            listingOfflineFinder,
            new SimpleMisspellingParser()
        );
        ComposedMisspellingLoader composedMisspellingLoader = new ComposedMisspellingLoader(
            listingOfflineFinder,
            new ComposedMisspellingParser()
        );
        uppercaseFinder = new UppercaseFinder(simpleMisspellingLoader, composedMisspellingLoader);
        uppercaseFinder.init();
        templateFinder = new TemplateFinder(finderProperties, uppercaseFinder);
        templateFinder.initTemplateParams();
        titleFinder = new TitleFinder();
        tableFinder = new TableFinder();
        urlFinder = new UrlFinder();
        xmlTagFinder = new XmlTagFinder();

        // Load listings
        falsePositiveLoader.load();
        simpleMisspellingLoader.load();
        composedMisspellingLoader.load();
    }

    @TearDown
    public void tearDown() {
        context.close();
    }

    @Benchmark
    public void commentFinder(Blackhole bh) {
        runFinder(commentFinder, bh);
    }

    @Benchmark
    public void completeTagFinder(Blackhole bh) {
        runFinder(completeTagFinder, bh);
    }

    @Benchmark
    public void cursiveFinder(Blackhole bh) {
        runFinder(cursiveFinder, bh);
    }

    @Benchmark
    public void falsePositiveFinder(Blackhole bh) {
        runFinder(falsePositiveFinder, bh);
    }

    @Benchmark
    public void ignorableSectionFinder(Blackhole bh) {
        runFinder(ignorableSectionFinder, bh);
    }

    @Benchmark
    public void redirectionFinder(Blackhole bh) {
        runFinder(redirectionFinder, bh);
    }

    @Benchmark
    public void linkFinder(Blackhole bh) {
        runFinder(linkFinder, bh);
    }

    @Benchmark
    public void personNameFinder(Blackhole bh) {
        runFinder(personNameFinder, bh);
    }

    @Benchmark
    public void personSurnameFinder(Blackhole bh) {
        runFinder(personSurnameFinder, bh);
    }

    @Benchmark
    public void quotesAngularFinder(Blackhole bh) {
        runFinder(quotesAngularFinder, bh);
    }

    @Benchmark
    public void quotesDoubleFinder(Blackhole bh) {
        runFinder(quotesDoubleFinder, bh);
    }

    @Benchmark
    public void quotesTypographicFinder(Blackhole bh) {
        runFinder(quotesTypographicFinder, bh);
    }

    @Benchmark
    public void templateFinder(Blackhole bh) {
        runFinder(templateFinder, bh);
    }

    @Benchmark
    public void titleFinder(Blackhole bh) {
        runFinder(titleFinder, bh);
    }

    @Benchmark
    public void tableFinder(Blackhole bh) {
        runFinder(tableFinder, bh);
    }

    @Benchmark
    public void uppercaseFinder(Blackhole bh) {
        runFinder(uppercaseFinder, bh);
    }

    @Benchmark
    public void urlFinder(Blackhole bh) {
        runFinder(urlFinder, bh);
    }

    @Benchmark
    public void xmlTagFinder(Blackhole bh) {
        runFinder(xmlTagFinder, bh);
    }

    public static void main(String[] args) throws RunnerException {
        run(ImmutableFinderJmhBenchmarkTest.class, fileName);
    }
}
