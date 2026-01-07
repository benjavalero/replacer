package es.bvalero.replacer.finder.cosmetic;

import static es.bvalero.replacer.checkwikipedia.CheckWikipediaAction.NO_ACTION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.checkwikipedia.CheckWikipediaFixEvent;
import es.bvalero.replacer.finder.*;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

class CosmeticFinderServiceTest {

    // Dependency injection
    private CosmeticFinder cosmeticFinder;
    private ReplacementFindApi replacementFindApi;
    private ApplicationEventPublisher applicationEventPublisher;

    private CosmeticFinderService cosmeticFinderService;

    @BeforeEach
    void setUp() {
        cosmeticFinder = mock(CosmeticFinder.class);
        replacementFindApi = mock(ReplacementFindApi.class);
        applicationEventPublisher = mock(ApplicationEventPublisher.class);
        cosmeticFinderService = new CosmeticFinderService(
            List.of(cosmeticFinder),
            replacementFindApi,
            applicationEventPublisher
        );
    }

    @Test
    void testApplyCosmeticChanges() {
        Cosmetic cosmetic = new Cosmetic(2, "[[Link|link]]", "[[link]]", NO_ACTION);
        when(cosmeticFinder.find(any(FinderPage.class))).thenReturn(Stream.of(cosmetic)).thenReturn(Stream.empty());

        String text = "A [[Link|link]] to simplify.";
        String expected = "A [[link]] to simplify.";
        FinderPage page = buildFinderPage(text);
        assertEquals(page.withContent(expected), cosmeticFinderService.applyCosmeticChanges(page));

        verify(cosmeticFinder).find(page);
        verify(applicationEventPublisher).publishEvent(
            CheckWikipediaFixEvent.of(page.getPageKey().getLang(), page.getTitle(), cosmetic.checkWikipediaAction())
        );
    }

    private FinderPage buildFinderPage(String content) {
        return FinderPage.of(content);
    }

    @Test
    void testApplySeveralCosmeticChanges() {
        Cosmetic cosmetic = new Cosmetic(2, "[[Link|link]]", "[[link]]", NO_ACTION);
        Cosmetic cosmetic2 = new Cosmetic(29, "archivo", "Archivo", NO_ACTION);
        Cosmetic cosmetic3 = new Cosmetic(19, "</br>", "<br>", NO_ACTION);
        when(cosmeticFinder.find(any(FinderPage.class)))
            .thenReturn(Stream.of(cosmetic, cosmetic2, cosmetic3))
            .thenReturn(Stream.empty());

        String text = "A [[Link|link]] to </br> a [[archivo:x.jpeg]].";
        String expected = "A [[link]] to <br> a [[Archivo:x.jpeg]].";
        FinderPage page = buildFinderPage(text);
        assertEquals(page.withContent(expected), cosmeticFinderService.applyCosmeticChanges(page));

        verify(cosmeticFinder).find(page);
    }

    @Test
    void testApplyNestedCosmeticChanges() {
        Cosmetic cosmetic = new Cosmetic(2, "[[Link|''link'']]", "''[[Link|link]]''", NO_ACTION);
        Cosmetic cosmetic2 = new Cosmetic(4, "[[Link|link]]", "[[link]]", NO_ACTION);
        when(cosmeticFinder.find(any(FinderPage.class)))
            .thenReturn(Stream.of(cosmetic))
            .thenReturn(Stream.of(cosmetic2))
            .thenReturn(Stream.empty());

        String text = "A [[Link|''link'']].";
        String expected = "A ''[[link]]''.";
        FinderPage page = buildFinderPage(text);
        assertEquals(page.withContent(expected), cosmeticFinderService.applyCosmeticChanges(page));

        verify(cosmeticFinder).find(page);
    }

    @Test
    void testFindCosmetics() {
        // Given
        FinderPage page = buildFinderPage("Some text with [[Link|link]] and mistak.");
        Cosmetic cosmetic = new Cosmetic(15, "[[Link|link]]", "[[link]]", NO_ACTION);
        when(cosmeticFinder.find(page)).thenReturn(Stream.of(cosmetic));

        Replacement replacement = Replacement.of(
            31,
            "mistak",
            StandardType.of(ReplacementKind.SIMPLE, "mistak"),
            List.of(Suggestion.ofNoComment("mistake"))
        );
        when(replacementFindApi.findAutomaticReplacements(page)).thenReturn(new TreeSet<>(List.of(replacement)));

        // When
        List<Cosmetic> results = cosmeticFinderService.findCosmetics(page).stream().toList();

        // Then
        assertEquals(2, results.size());
        assertEquals(cosmetic, results.get(0));

        Cosmetic converted = results.get(1);
        assertEquals(31, converted.start());
        assertEquals("mistak", converted.text());
        assertEquals("mistake", converted.fix());
        assertEquals(NO_ACTION, converted.checkWikipediaAction());

        verify(cosmeticFinder).find(page);
        verify(replacementFindApi).findAutomaticReplacements(page);
    }
}
