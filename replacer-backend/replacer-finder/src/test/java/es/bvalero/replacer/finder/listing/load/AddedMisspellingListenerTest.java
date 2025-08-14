package es.bvalero.replacer.finder.listing.load;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.LangReplacementType;
import es.bvalero.replacer.finder.ReplacementKind;
import es.bvalero.replacer.finder.StandardType;
import es.bvalero.replacer.finder.listing.SimpleMisspelling;
import es.bvalero.replacer.finder.listing.StandardMisspelling;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections4.SetValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

class AddedMisspellingListenerTest {

    // Dependency injection
    private SimpleMisspellingLoader simpleMisspellingLoader;
    private ComposedMisspellingLoader composedMisspellingLoader;
    private ApplicationEventPublisher applicationEventPublisher;

    private AddedMisspellingListener addedMisspellingListener;

    @BeforeEach
    public void setUp() {
        simpleMisspellingLoader = mock(SimpleMisspellingLoader.class);
        composedMisspellingLoader = mock(ComposedMisspellingLoader.class);
        applicationEventPublisher = mock(ApplicationEventPublisher.class);
        addedMisspellingListener = new AddedMisspellingListener(
            simpleMisspellingLoader,
            composedMisspellingLoader,
            applicationEventPublisher
        );
    }

    @Test
    void testGetAddedMisspellings() {
        SimpleMisspelling misspelling1 = SimpleMisspelling.ofCaseInsensitive("A", "B");
        SimpleMisspelling misspelling2 = SimpleMisspelling.ofCaseInsensitive("B", "C");
        SetValuedMap<WikipediaLanguage, StandardMisspelling> map1 = new HashSetValuedHashMap<>();
        map1.putAll(WikipediaLanguage.getDefault(), List.of(misspelling1, misspelling2));

        SimpleMisspelling misspelling3 = SimpleMisspelling.ofCaseInsensitive("C", "D");
        SetValuedMap<WikipediaLanguage, StandardMisspelling> map2 = new HashSetValuedHashMap<>();
        map2.putAll(WikipediaLanguage.getDefault(), List.of(misspelling2, misspelling3));

        Set<LangReplacementType> expected = Set.of(
            LangReplacementType.of(WikipediaLanguage.getDefault(), StandardType.of(ReplacementKind.SIMPLE, "C"))
        );
        assertEquals(expected, new HashSet<>(addedMisspellingListener.getAddedMisspellings(map1, map2)));
    }

    @Test
    void testGetAddedMisspellingsFirstTime() {
        SetValuedMap<WikipediaLanguage, StandardMisspelling> map1 = new HashSetValuedHashMap<>();

        SimpleMisspelling misspelling = SimpleMisspelling.ofCaseInsensitive("C", "D");
        SetValuedMap<WikipediaLanguage, StandardMisspelling> map2 = new HashSetValuedHashMap<>();
        map2.putAll(WikipediaLanguage.getDefault(), List.of(misspelling));

        assertTrue(addedMisspellingListener.getAddedMisspellings(map1, map2).isEmpty());
    }
}
