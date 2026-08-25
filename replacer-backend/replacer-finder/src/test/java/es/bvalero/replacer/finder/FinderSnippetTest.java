package es.bvalero.replacer.finder;

import es.bvalero.replacer.FinderProperties;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.finder.immutable.ImmutableFinderService;
import es.bvalero.replacer.finder.immutable.finders.*;
import es.bvalero.replacer.finder.listing.find.ListingOfflineFinder;
import es.bvalero.replacer.finder.listing.load.ComposedMisspellingLoader;
import es.bvalero.replacer.finder.listing.load.FalsePositiveLoader;
import es.bvalero.replacer.finder.listing.load.SimpleMisspellingLoader;
import es.bvalero.replacer.finder.listing.parse.ComposedMisspellingParser;
import es.bvalero.replacer.finder.listing.parse.FalsePositiveParser;
import es.bvalero.replacer.finder.listing.parse.SimpleMisspellingParser;
import es.bvalero.replacer.finder.replacement.ReplacementFinderService;
import es.bvalero.replacer.finder.replacement.finders.*;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * End-to-end test for finding replacements in hardcoded text snippets.
 * Useful for quickly testing specific cases without needing a full article file.
 */
@EnableConfigurationProperties(FinderProperties.class)
@SpringBootTest(classes = FinderProperties.class)
class FinderSnippetTest {

    private static ReplacementFinderService replacementFinderService;

    @BeforeAll
    static void setUp(@Autowired FinderProperties finderProperties) throws ReplacerException {
        ListingOfflineFinder listingFinder = new ListingOfflineFinder();

        SimpleMisspellingLoader simpleMisspellingLoader = new SimpleMisspellingLoader(
            listingFinder,
            new SimpleMisspellingParser()
        );
        ComposedMisspellingLoader composedMisspellingLoader = new ComposedMisspellingLoader(
            listingFinder,
            new ComposedMisspellingParser()
        );
        FalsePositiveLoader falsePositiveLoader = new FalsePositiveLoader(listingFinder, new FalsePositiveParser());

        UppercaseFinder uppercaseFinder = new UppercaseFinder(simpleMisspellingLoader, composedMisspellingLoader);
        uppercaseFinder.init();
        CompleteTagFinder completeTagFinder = new CompleteTagFinder(finderProperties);
        completeTagFinder.init();
        FalsePositiveFinder falsePositiveFinder = new FalsePositiveFinder(falsePositiveLoader);
        falsePositiveFinder.init();
        IgnorableSectionFinder ignorableSectionFinder = new IgnorableSectionFinder(finderProperties);
        ignorableSectionFinder.init();
        LinkFinder linkFinder = new LinkFinder(finderProperties, uppercaseFinder);
        linkFinder.init();
        PersonNameFinder personNameFinder = new PersonNameFinder(finderProperties);
        personNameFinder.init();
        PersonSurnameFinder personSurnameFinder = new PersonSurnameFinder(finderProperties);
        personSurnameFinder.init();
        RedirectionFinder redirectionFinder = new RedirectionFinder(finderProperties);
        redirectionFinder.init();
        TemplateFinder templateFinder = new TemplateFinder(finderProperties, uppercaseFinder);
        templateFinder.initTemplateParams();

        ImmutableFinderService immutableFinderService = new ImmutableFinderService(
            new ArrayList<>(
                List.of(
                    new CommentFinder(),
                    completeTagFinder,
                    new CursiveFinder(),
                    falsePositiveFinder,
                    ignorableSectionFinder,
                    linkFinder,
                    personNameFinder,
                    personSurnameFinder,
                    new QuotesAngularFinder(),
                    new QuotesDoubleFinder(),
                    new QuotesTypographicFinder(),
                    redirectionFinder,
                    new TableFinder(),
                    templateFinder,
                    new TitleFinder(),
                    uppercaseFinder,
                    new UrlFinder(),
                    new XmlTagFinder()
                )
            )
        );
        immutableFinderService.sortImmutableFinders();

        DateFinder dateFinder = new DateFinder(finderProperties);
        dateFinder.init();
        OrdinalFinder ordinalFinder = new OrdinalFinder(finderProperties);
        ordinalFinder.init();
        MisspellingSimpleFinder misspellingSimpleFinder = new MisspellingSimpleFinder(simpleMisspellingLoader);
        misspellingSimpleFinder.init();
        MisspellingComposedFinder misspellingComposedFinder = new MisspellingComposedFinder(composedMisspellingLoader);
        misspellingComposedFinder.init();

        replacementFinderService = new ReplacementFinderService(
            new ArrayList<>(
                List.of(
                    new AcuteOFinder(),
                    new CenturyFinder(),
                    new CoordinatesFinder(),
                    dateFinder,
                    new DegreeFinder(),
                    ordinalFinder,
                    misspellingSimpleFinder,
                    misspellingComposedFinder
                )
            ),
            immutableFinderService
        );
        replacementFinderService.sortReplacementFinders();

        falsePositiveLoader.load();
        simpleMisspellingLoader.load();
        composedMisspellingLoader.load();
    }

    @Test
    void findReplacementsInSnippet() {
        // Change this text to test specific cases
        String content = """
            El 2 de Septiembre de 2019 se celebró en angulo un evento
            dedicado a la 2ª edicion de un torneo internacional. El palmares
            del equipo incluía varios titulos, entre ellos el campeonato
            de la Segunda División obtenido en la temporada 2018-2019.
            Tambien participó ramon Garcia, un conocido deportista que
            habia ganado medallas en diversas competiciones a lo largo
            de su dilatada carrera profesional.
            """;

        FinderPage page = FinderPage.of("Test snippet", content);
        SortedSet<Replacement> replacements = replacementFinderService.findReplacements(page);

        printReplacements(content, replacements);
    }

    private static void printReplacements(String content, SortedSet<Replacement> replacements) {
        System.out.println("═".repeat(80));
        System.out.println("TEXT: " + content);
        System.out.println("═".repeat(80));
        System.out.printf("Found %d replacement(s):%n%n", replacements.size());

        int index = 1;
        for (Replacement r : replacements) {
            int end = r.start() + r.text().length();
            String context = getContext(content, r.start(), end);

            System.out.printf("  %d. «%s» [%d-%d]%n", index, r.text(), r.start(), end);
            System.out.printf("     Type: %s%n", r.type());
            System.out.printf("     Context: %s%n", context);
            if (!r.suggestions().isEmpty()) {
                System.out.println("     Suggestions:");
                for (Suggestion s : r.suggestions()) {
                    String marker = s.getText().equals(r.text()) ? " (original)" : "";
                    String comment = s.getComment() != null ? " — " + s.getComment() : "";
                    System.out.printf("       • %s%s%s%n", s.getText(), comment, marker);
                }
            }
            System.out.println();
            index++;
        }
        System.out.println("═".repeat(80));
    }

    /** Returns a short context around the replacement, highlighting it */
    private static String getContext(String content, int start, int end) {
        int ctxStart = Math.max(0, start - 20);
        int ctxEnd = Math.min(content.length(), end + 20);
        String before = content.substring(ctxStart, start);
        String match = content.substring(start, end);
        String after = content.substring(end, ctxEnd);
        return (
            (ctxStart > 0 ? "…" : "") + before + ">>>" + match + "<<<" + after + (ctxEnd < content.length() ? "…" : "")
        );
    }
}
