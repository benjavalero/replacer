package es.bvalero.replacer.finder.cosmetic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.checkwikipedia.CheckWikipediaFixEvent;
import es.bvalero.replacer.finder.Cosmetic;
import es.bvalero.replacer.finder.FinderPage;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

class CosmeticFinderServiceTest {

    // Dependency injection
    private CosmeticFinder cosmeticFinder;
    private ApplicationEventPublisher applicationEventPublisher;

    private CosmeticFinderService cosmeticFinderService;

    @BeforeEach
    void setUp() {
        cosmeticFinder = mock(CosmeticFinder.class);
        applicationEventPublisher = mock(ApplicationEventPublisher.class);
        cosmeticFinderService = new CosmeticFinderService(List.of(cosmeticFinder), applicationEventPublisher);
    }

    @Test
    void testApplyCosmeticChanges() {
        Cosmetic cosmetic = Cosmetic.builder().start(2).text("[[Link|link]]").fix("[[link]]").build();
        when(cosmeticFinder.find(any(FinderPage.class)))
            .thenReturn(Set.of(cosmetic))
            .thenReturn(Collections.emptySet());

        String text = "A [[Link|link]] to simplify.";
        String expected = "A [[link]] to simplify.";
        FinderPage page = buildFinderPage(text);
        assertEquals(page.withContent(expected), cosmeticFinderService.applyCosmeticChanges(page));

        verify(cosmeticFinder).find(page);
        verify(applicationEventPublisher).publishEvent(
            CheckWikipediaFixEvent.of(page.getPageKey().getLang(), page.getTitle(), cosmetic.getCheckWikipediaAction())
        );
    }

    private FinderPage buildFinderPage(String content) {
        return FinderPage.of(content);
    }

    @Test
    void testApplySeveralCosmeticChanges() {
        Cosmetic cosmetic = Cosmetic.builder().start(2).text("[[Link|link]]").fix("[[link]]").build();
        Cosmetic cosmetic2 = Cosmetic.builder().start(29).text("archivo").fix("Archivo").build();
        Cosmetic cosmetic3 = Cosmetic.builder().start(19).text("</br>").fix("<br>").build();
        when(cosmeticFinder.find(any(FinderPage.class)))
            .thenReturn(Set.of(cosmetic, cosmetic2, cosmetic3))
            .thenReturn(Collections.emptySet());

        String text = "A [[Link|link]] to </br> a [[archivo:x.jpeg]].";
        String expected = "A [[link]] to <br> a [[Archivo:x.jpeg]].";
        FinderPage page = buildFinderPage(text);
        assertEquals(page.withContent(expected), cosmeticFinderService.applyCosmeticChanges(page));

        verify(cosmeticFinder).find(page);
    }

    @Test
    void testApplyNestedCosmeticChanges() {
        Cosmetic cosmetic = Cosmetic.builder().start(2).text("[[Link|''link'']]").fix("''[[Link|link]]''").build();
        Cosmetic cosmetic2 = Cosmetic.builder().start(4).text("[[Link|link]]").fix("[[link]]").build();
        when(cosmeticFinder.find(any(FinderPage.class)))
            .thenReturn(Set.of(cosmetic))
            .thenReturn(Set.of(cosmetic2))
            .thenReturn(Collections.emptySet());

        String text = "A [[Link|''link'']].";
        String expected = "A ''[[link]]''.";
        FinderPage page = buildFinderPage(text);
        assertEquals(page.withContent(expected), cosmeticFinderService.applyCosmeticChanges(page));

        verify(cosmeticFinder).find(page);
    }
}
