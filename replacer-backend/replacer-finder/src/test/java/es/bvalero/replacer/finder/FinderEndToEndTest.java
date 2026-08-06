package es.bvalero.replacer.finder;

import es.bvalero.replacer.FinderProperties;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.common.util.FileOfflineUtils;
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

@EnableConfigurationProperties(FinderProperties.class)
@SpringBootTest(classes = FinderProperties.class)
class FinderEndToEndTest {

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
    void findReplacementsInTestPage() throws ReplacerException {
        String content = FileOfflineUtils.getFileContent("test-page.txt");
        FinderPage page = FinderPage.of("Test page", content);

        SortedSet<Replacement> replacements = replacementFinderService.findReplacements(page);

        replacements.forEach(r ->
            System.out.printf(
                "[%d-%d] %s (%s) → %s%n",
                r.start(),
                r.start() + r.text().length(),
                r.text(),
                r.type(),
                r.suggestions()
            )
        );
        System.out.printf("%nTotal: %d replacements%n", replacements.size());
    }
}
