package es.bvalero.replacer.finder.replacement.finders;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.finder.benchmark.BaseFinderJmhBenchmark;
import es.bvalero.replacer.finder.listing.ComposedMisspelling;
import es.bvalero.replacer.finder.listing.SimpleMisspelling;
import es.bvalero.replacer.finder.listing.find.ListingFinder;
import es.bvalero.replacer.finder.listing.find.ListingOfflineFinder;
import es.bvalero.replacer.finder.listing.load.ComposedMisspellingLoader;
import es.bvalero.replacer.finder.listing.load.SimpleMisspellingLoader;
import es.bvalero.replacer.finder.listing.parse.ComposedMisspellingParser;
import es.bvalero.replacer.finder.listing.parse.SimpleMisspellingParser;
import es.bvalero.replacer.wikipedia.WikipediaException;
import java.beans.PropertyChangeEvent;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections4.SetValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

@State(Scope.Benchmark)
public class ReplacementFinderJmhBenchmark extends BaseFinderJmhBenchmark {

    private final AcuteOFinder acuteOFinder = new AcuteOFinder();
    private final CenturyFinder centuryFinder = new CenturyFinder();
    private final CoordinatesFinder coordinatesFinder = new CoordinatesFinder();
    private final DateFinder dateFinder = new DateFinder();
    private final ComposedMisspellingLoader composedMisspellingLoader = new ComposedMisspellingLoader();
    private final MisspellingComposedFinder misspellingComposedFinder = new MisspellingComposedFinder();
    private final SimpleMisspellingLoader simpleMisspellingLoader = new SimpleMisspellingLoader();
    private final MisspellingSimpleFinder misspellingSimpleFinder = new MisspellingSimpleFinder();

    @Setup
    public void setUp() throws WikipediaException, ReplacerException {
        super.setUp();

        // Date finder configuration
        final String lang = WikipediaLanguage.getDefault().getCode();
        this.dateFinder.setMonthNames(
                Map.of(
                    lang,
                    "enero,febrero,marzo,abril,mayo,junio,julio,agosto,septiembre,setiembre,octubre,noviembre,diciembre"
                )
            );
        this.dateFinder.setDateConnectors(Map.of(lang, "a,desde,durante,el,entre,en,hacia,hasta,para,y"));
        this.dateFinder.setYearPrepositions(Map.of(lang, "de,del"));
        this.dateFinder.setDateArticles(
                Map.of(
                    lang,
                    "a-al,desde-desde el,de-del,durante-durante el,entre-entre el,en-el,hacia-hacia el,hasta-hasta el,y-y el"
                )
            );
        this.dateFinder.init();

        final ListingFinder listingFinder = new ListingOfflineFinder();

        // Composed misspelling finder
        this.misspellingComposedFinder.setComposedMisspellingLoader(composedMisspellingLoader);
        composedMisspellingLoader.setComposedMisspellingParser(new ComposedMisspellingParser());
        Set<ComposedMisspelling> composedMisspellings = composedMisspellingLoader.parseListing(
            listingFinder.getComposedMisspellingListing(WikipediaLanguage.getDefault())
        );
        fakeUpdateComposedMisspellings(composedMisspellings);

        // Simple misspelling finder
        this.misspellingSimpleFinder.setSimpleMisspellingLoader(simpleMisspellingLoader);
        SimpleMisspellingLoader simpleMisspellingLoader = new SimpleMisspellingLoader();
        simpleMisspellingLoader.setSimpleMisspellingParser(new SimpleMisspellingParser());
        Set<SimpleMisspelling> simpleMisspellings = simpleMisspellingLoader.parseListing(
            listingFinder.getSimpleMisspellingListing(WikipediaLanguage.getDefault())
        );
        fakeUpdateSimpleMisspellings(simpleMisspellings);
    }

    private void fakeUpdateComposedMisspellings(Set<ComposedMisspelling> misspellings) {
        SetValuedMap<WikipediaLanguage, ComposedMisspelling> map = new HashSetValuedHashMap<>();
        map.putAll(WikipediaLanguage.getDefault(), misspellings);

        composedMisspellingLoader.setItems(map);
        SetValuedMap<WikipediaLanguage, ComposedMisspelling> emptyMap = new HashSetValuedHashMap<>();
        misspellingComposedFinder.propertyChange(
            new PropertyChangeEvent(this, ComposedMisspellingLoader.PROPERTY_ITEMS, emptyMap, map)
        );
    }

    private void fakeUpdateSimpleMisspellings(Set<SimpleMisspelling> misspellings) {
        SetValuedMap<WikipediaLanguage, SimpleMisspelling> map = new HashSetValuedHashMap<>();
        map.putAll(WikipediaLanguage.getDefault(), misspellings);

        simpleMisspellingLoader.setItems(map);
        SetValuedMap<WikipediaLanguage, SimpleMisspelling> emptyMap = new HashSetValuedHashMap<>();
        misspellingSimpleFinder.propertyChange(
            new PropertyChangeEvent(this, SimpleMisspellingLoader.PROPERTY_ITEMS, emptyMap, map)
        );
    }

    @Benchmark
    public void testAcuteOFinder() {
        runFinder(acuteOFinder);
    }

    @Benchmark
    public void testCenturyFinder() {
        runFinder(centuryFinder);
    }

    @Benchmark
    public void testCoordinatesFinder() {
        runFinder(coordinatesFinder);
    }

    @Benchmark
    public void testDateFinder() {
        runFinder(dateFinder);
    }

    @Benchmark
    public void testMisspellingComposedFinder() {
        runFinder(misspellingComposedFinder);
    }

    @Benchmark
    public void testMisspellingSimpleFinder() {
        runFinder(misspellingSimpleFinder);
    }
}
