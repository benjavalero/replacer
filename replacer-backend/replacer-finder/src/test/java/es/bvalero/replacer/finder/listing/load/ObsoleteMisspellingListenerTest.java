package es.bvalero.replacer.finder.listing.load;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.listing.SimpleMisspelling;
import es.bvalero.replacer.finder.listing.StandardMisspelling;
import es.bvalero.replacer.replacement.type.ReplacementTypeSaveApi;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections4.SetValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ObsoleteMisspellingListenerTest {

    // Dependency injection
    private SimpleMisspellingLoader simpleMisspellingLoader;
    private ComposedMisspellingLoader composedMisspellingLoader;
    private ReplacementTypeSaveApi replacementTypeSaveApi;

    private ObsoleteMisspellingListener obsoleteMisspellingListener;

    @BeforeEach
    public void setUp() {
        simpleMisspellingLoader = mock(SimpleMisspellingLoader.class);
        composedMisspellingLoader = mock(ComposedMisspellingLoader.class);
        replacementTypeSaveApi = mock(ReplacementTypeSaveApi.class);
        obsoleteMisspellingListener = new ObsoleteMisspellingListener(
            simpleMisspellingLoader,
            composedMisspellingLoader,
            replacementTypeSaveApi
        );
    }

    @Test
    void testGetObsoleteMisspellings() {
        SimpleMisspelling misspelling1 = SimpleMisspelling.ofCaseInsensitive("A", "B");
        SimpleMisspelling misspelling2 = SimpleMisspelling.ofCaseInsensitive("B", "C");
        SetValuedMap<WikipediaLanguage, StandardMisspelling> map1 = new HashSetValuedHashMap<>();
        map1.putAll(WikipediaLanguage.getDefault(), List.of(misspelling1, misspelling2));

        SimpleMisspelling misspelling3 = SimpleMisspelling.ofCaseInsensitive("C", "D");
        SetValuedMap<WikipediaLanguage, StandardMisspelling> map2 = new HashSetValuedHashMap<>();
        map2.putAll(WikipediaLanguage.getDefault(), List.of(misspelling2, misspelling3));

        Set<ChangedReplacementType> expected = Set.of(
            ChangedReplacementType.of(WikipediaLanguage.getDefault(), StandardType.of(ReplacementKind.SIMPLE, "A"))
        );
        assertEquals(expected, new HashSet<>(obsoleteMisspellingListener.getObsoleteMisspellings(map1, map2)));
    }
}
