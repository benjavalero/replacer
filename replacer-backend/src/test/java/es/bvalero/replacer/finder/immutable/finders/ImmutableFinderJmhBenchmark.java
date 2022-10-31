package es.bvalero.replacer.finder.immutable.finders;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.finder.benchmark.BaseFinderJmhBenchmark;
import es.bvalero.replacer.finder.listing.FalsePositive;
import es.bvalero.replacer.finder.listing.SimpleMisspelling;
import es.bvalero.replacer.finder.listing.find.ListingFinder;
import es.bvalero.replacer.finder.listing.find.ListingOfflineFinder;
import es.bvalero.replacer.finder.listing.load.ComposedMisspellingLoader;
import es.bvalero.replacer.finder.listing.load.FalsePositiveLoader;
import es.bvalero.replacer.finder.listing.load.SimpleMisspellingLoader;
import es.bvalero.replacer.finder.listing.parse.FalsePositiveParser;
import es.bvalero.replacer.finder.listing.parse.SimpleMisspellingParser;
import es.bvalero.replacer.wikipedia.WikipediaException;
import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections4.SetValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

@State(Scope.Benchmark)
public class ImmutableFinderJmhBenchmark extends BaseFinderJmhBenchmark {

    private final CommentFinder commentFinder = new CommentFinder();
    private final CompleteTagFinder completeTagFinder = new CompleteTagFinder();
    private final CursiveFinder cursiveFinder = new CursiveFinder();
    private final FalsePositiveLoader falsePositiveLoader = new FalsePositiveLoader();
    private final FalsePositiveFinder falsePositiveFinder = new FalsePositiveFinder();
    private final IgnorableSectionFinder ignorableSectionFinder = new IgnorableSectionFinder();
    private final IgnorableTemplateFinder ignorableTemplateFinder = new IgnorableTemplateFinder();
    private final LinkFinder linkFinder = new LinkFinder();
    private final PersonNameFinder personNameFinder = new PersonNameFinder();
    private final PersonSurnameFinder personSurnameFinder = new PersonSurnameFinder();
    private final QuotesAngularFinder quotesAngularFinder = new QuotesAngularFinder();
    private final QuotesDoubleFinder quotesDoubleFinder = new QuotesDoubleFinder();
    private final QuotesTypographicFinder quotesTypographicFinder = new QuotesTypographicFinder();
    private final TableFinder tableFinder = new TableFinder();
    private final TemplateFinder templateFinder = new TemplateFinder();
    private final TitleFinder titleFinder = new TitleFinder();
    private final SimpleMisspellingLoader simpleMisspellingLoader = new SimpleMisspellingLoader();
    private final UppercaseFinder uppercaseFinder = new UppercaseFinder();
    private final UrlFinder urlFinder = new UrlFinder();
    private final XmlTagFinder xmlTagFinder = new XmlTagFinder();

    @Setup
    public void setUp() throws WikipediaException, ReplacerException {
        super.setUp();

        // Complete Tag finder configuration
        this.completeTagFinder.setCompleteTags(
                Set.of(
                    "ref",
                    "gallery",
                    "blockquote",
                    "nowiki",
                    "math",
                    "cite",
                    "source",
                    "code",
                    "poem",
                    "pre",
                    "tt",
                    "syntaxhighlight",
                    "score"
                )
            );

        // Ignorable Section finder configuration
        this.ignorableSectionFinder.setIgnorableSections(Set.of("Bibliografía", "Enlaces externos", "Publicaciones"));

        // Ignorable Template finder configuration
        this.ignorableTemplateFinder.setIgnorableTemplates(
                List.of(
                    "#redirect",
                    "#redirección",
                    "#redireccion",
                    "{{destruir",
                    "{{delete",
                    "{{borrar",
                    "{{eliminar",
                    "{{copyedit",
                    "{{ortografía",
                    "{{problemas artículo",
                    "{{problemas",
                    "{{pa",
                    "{{nobots",
                    "{{en uso",
                    "{{enuso",
                    "{{en desarrollo",
                    "{{endesarrollo",
                    "{{en obras",
                    "{{enobras",
                    "{{en construcción",
                    "{{traducción",
                    "{{mal traducido",
                    "{{maltraducido",
                    "{{mala traducción",
                    "{{malatraducción",
                    "{{traducción defectuosa",
                    "{{traducción incompleta",
                    "{{traducción inconclusa",
                    "{{autotrad",
                    "{{revisar traducción",
                    "{{wikificar"
                )
            );

        // Person Name finder configuration
        this.personNameFinder.setPersonNames(
                Set.of(
                    "Basket",
                    "Canada",
                    "Division",
                    "Domingo",
                    "Frances",
                    "Julio de la",
                    "Julio de",
                    "Julio",
                    "Junio",
                    "Latin",
                    "Los Angeles",
                    "Manchester",
                    "Master",
                    "Masters",
                    "Milan",
                    "Missouri",
                    "Octubre",
                    "Online",
                    "Sky",
                    "Sidney",
                    "Television",
                    "Tokyo"
                )
            );

        // Person Surname finder configuration
        this.personSurnameFinder.setPersonSurnames(
                Set.of(
                    "([0-9]{1,2} )?de (Enero|Febrero|Marzo|Abril|Mayo|Junio|Julio|Agosto|Septiembre|Octubre|Noviembre|Diciembre)",
                    "Abril",
                    "*Airlines",
                    "*Airways",
                    "Albums",
                    "America",
                    "Basket",
                    "Canada",
                    "Carter",
                    "de Catalunya",
                    "Comics",
                    "Domingo",
                    "de Invierno",
                    "Julio",
                    "Junio",
                    "Latin",
                    "*League",
                    "Link",
                    "Master",
                    "Masters",
                    "Match",
                    "Mayo",
                    "Online",
                    "Pinto",
                    "Records",
                    "Sky",
                    "Television",
                    "Union",
                    "de Verano",
                    "Version"
                )
            );

        final ListingFinder listingFinder = new ListingOfflineFinder();

        // False Positive finder configuration
        this.falsePositiveFinder.setFalsePositiveLoader(falsePositiveLoader);
        this.falsePositiveLoader.setFalsePositiveParser(new FalsePositiveParser());
        final Set<FalsePositive> falsePositives = falsePositiveLoader.parseListing(
            listingFinder.getFalsePositiveListing(WikipediaLanguage.getDefault())
        );
        fakeUpdateFalsePositives(falsePositives);

        // Uppercase finder configuration
        this.uppercaseFinder.setSimpleMisspellingLoader(simpleMisspellingLoader);
        this.simpleMisspellingLoader.setSimpleMisspellingParser(new SimpleMisspellingParser());
        final Set<SimpleMisspelling> simpleMisspellings = simpleMisspellingLoader.parseListing(
            listingFinder.getSimpleMisspellingListing(WikipediaLanguage.getDefault())
        );
        fakeUpdateSimpleMisspellings(simpleMisspellings);

        // Template finder configuration
        this.templateFinder.setTemplateParams(
                List.of(
                    "bandera*|*",
                    "cita*|*",
                    "cite*|*",
                    "caja de cita|*",
                    "commons|*",
                    "commonscat|*",
                    "convertir|*",
                    "coord|*",
                    "DEFAULTSORT|*",
                    "enlace roto|*",
                    "facebook|*",
                    "ficha de taxón|*",
                    "Fila BIC|*",
                    "Fila LIC|*",
                    "flag*|*",
                    "harvnb|*",
                    "harvnp|*",
                    "imdb*|*",
                    "lang*|*",
                    "link|*",
                    "NF|*",
                    "nihongo|*",
                    "obra citada|*",
                    "ORDENAR|*",
                    "quote|*",
                    "refn|*",
                    "sort|*",
                    "sortname|*",
                    "sfn|*",
                    "taxobox|*",
                    "traducido ref|*",
                    "twitter|*",
                    "url|*",
                    "versalita|*",
                    "wayback|*",
                    "webarchive|*",
                    "Wikidata list*|*",
                    "#expr|*",
                    "#invoke|*",
                    "#tag|*",
                    "*|cita",
                    "*|escudo",
                    "*|escudo2",
                    "*|escudo3",
                    "*|escudo4",
                    "*|escudo5",
                    "*|facebook",
                    "*|imagen",
                    "*|imaxe",
                    "*|índice",
                    "*|mapa",
                    "*|página web",
                    "*|reporte",
                    "*|reporte2",
                    "*|romaji",
                    "*|url",
                    "AllMusic|class",
                    "Certification Table Entry|type",
                    "fs player|nat",
                    "Identificador carretera española|tipo",
                    "Jugador de fútbol|nat",
                    "Medallero|var"
                )
            );
        this.templateFinder.setUppercaseFinder(uppercaseFinder);
    }

    private void fakeUpdateFalsePositives(Set<FalsePositive> falsePositives) {
        SetValuedMap<WikipediaLanguage, FalsePositive> map = new HashSetValuedHashMap<>();
        map.putAll(WikipediaLanguage.getDefault(), falsePositives);

        falsePositiveLoader.setItems(map);
        SetValuedMap<WikipediaLanguage, FalsePositive> emptyMap = new HashSetValuedHashMap<>();
        falsePositiveFinder.propertyChange(
            new PropertyChangeEvent(this, ComposedMisspellingLoader.PROPERTY_ITEMS, emptyMap, map)
        );
    }

    private void fakeUpdateSimpleMisspellings(Set<SimpleMisspelling> misspellings) {
        SetValuedMap<WikipediaLanguage, SimpleMisspelling> map = new HashSetValuedHashMap<>();
        map.putAll(WikipediaLanguage.getDefault(), misspellings);

        simpleMisspellingLoader.setItems(map);
        SetValuedMap<WikipediaLanguage, SimpleMisspelling> emptyMap = new HashSetValuedHashMap<>();
        uppercaseFinder.propertyChange(
            new PropertyChangeEvent(this, SimpleMisspellingLoader.PROPERTY_ITEMS, emptyMap, map)
        );
    }

    @Benchmark
    public void testCommentFinder() {
        runFinder(commentFinder);
    }

    @Benchmark
    public void testCompleteTagFinder() {
        runFinder(completeTagFinder);
    }

    @Benchmark
    public void testCursiveFinder() {
        runFinder(cursiveFinder);
    }

    @Benchmark
    public void testFalsePositiveFinder() {
        runFinder(falsePositiveFinder);
    }

    @Benchmark
    public void testIgnorableSectionFinder() {
        runFinder(ignorableSectionFinder);
    }

    @Benchmark
    public void testIgnorableTemplateFinder() {
        runFinder(ignorableTemplateFinder);
    }

    @Benchmark
    public void testLinkFinder() {
        runFinder(linkFinder);
    }

    @Benchmark
    public void testPersonNameFinder() {
        runFinder(personNameFinder);
    }

    @Benchmark
    public void testPersonSurnameFinder() {
        runFinder(personSurnameFinder);
    }

    @Benchmark
    public void testQuotesAngularFinder() {
        runFinder(quotesAngularFinder);
    }

    @Benchmark
    public void testQuotesDoubleFinder() {
        runFinder(quotesDoubleFinder);
    }

    @Benchmark
    public void testQuotesTypographicFinder() {
        runFinder(quotesTypographicFinder);
    }

    @Benchmark
    public void testTableFinder() {
        runFinder(tableFinder);
    }

    @Benchmark
    public void testTemplateFinder() {
        runFinder(templateFinder);
    }

    @Benchmark
    public void testTitleFinder() {
        runFinder(titleFinder);
    }

    @Benchmark
    public void testUppercaseFinder() {
        runFinder(uppercaseFinder);
    }

    @Benchmark
    public void testUrlFinder() {
        runFinder(urlFinder);
    }

    @Benchmark
    public void testXmlTagFinder() {
        runFinder(xmlTagFinder);
    }
}
