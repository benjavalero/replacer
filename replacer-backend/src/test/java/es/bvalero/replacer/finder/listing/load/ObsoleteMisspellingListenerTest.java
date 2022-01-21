package es.bvalero.replacer.finder.listing.load;

import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.listing.SimpleMisspelling;
import es.bvalero.replacer.finder.replacement.RemoveObsoleteReplacementType;
import java.beans.PropertyChangeEvent;
import java.util.Collections;
import java.util.List;
import org.apache.commons.collections4.SetValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ObsoleteMisspellingListenerTest {

    private static final SetValuedMap<WikipediaLanguage, String> EMPTY_MAP = new HashSetValuedHashMap<>();

    @Mock
    private RemoveObsoleteReplacementType removeObsoleteReplacementType;

    @InjectMocks
    private ObsoleteMisspellingListener obsoleteMisspellingListener;

    @BeforeEach
    public void setUp() {
        obsoleteMisspellingListener = new ObsoleteMisspellingListener();
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testInitialLoad() {
        SimpleMisspelling misspelling1 = SimpleMisspelling.ofCaseInsensitive("A", "B");
        SimpleMisspelling misspelling2 = SimpleMisspelling.ofCaseInsensitive("B", "C");
        SetValuedMap<WikipediaLanguage, SimpleMisspelling> map1 = new HashSetValuedHashMap<>();
        map1.putAll(WikipediaLanguage.getDefault(), List.of(misspelling1, misspelling2));

        // Fake the update of the list in the manager
        obsoleteMisspellingListener.propertyChange(
            new PropertyChangeEvent(this, SimpleMisspellingLoader.PROPERTY_ITEMS, EMPTY_MAP, map1)
        );

        verify(removeObsoleteReplacementType, never())
            .removeObsoleteReplacementTypes(eq(WikipediaLanguage.getDefault()), anyCollection());
    }

    @Test
    void testDeleteObsoleteMisspellings() {
        SimpleMisspelling misspelling1 = SimpleMisspelling.ofCaseInsensitive("A", "B");
        SimpleMisspelling misspelling2 = SimpleMisspelling.ofCaseInsensitive("B", "C");
        SetValuedMap<WikipediaLanguage, SimpleMisspelling> map1 = new HashSetValuedHashMap<>();
        map1.putAll(WikipediaLanguage.getDefault(), List.of(misspelling1, misspelling2));

        SimpleMisspelling misspelling3 = SimpleMisspelling.ofCaseInsensitive("C", "D");
        SetValuedMap<WikipediaLanguage, SimpleMisspelling> map2 = new HashSetValuedHashMap<>();
        map2.putAll(WikipediaLanguage.getDefault(), List.of(misspelling2, misspelling3));

        // Fake the update of the list in the manager
        obsoleteMisspellingListener.propertyChange(
            new PropertyChangeEvent(this, SimpleMisspellingLoader.PROPERTY_ITEMS, map1, map2)
        );

        verify(removeObsoleteReplacementType)
            .removeObsoleteReplacementTypes(
                WikipediaLanguage.getDefault(),
                Collections.singleton(ReplacementType.of(ReplacementKind.SIMPLE, "A"))
            );
    }

    @Test
    void testMisspellingsWithDifferentSuggestions() {
        SimpleMisspelling misspelling1 = SimpleMisspelling.ofCaseInsensitive("A", "B");
        SimpleMisspelling misspelling2 = SimpleMisspelling.ofCaseInsensitive("B", "C");
        SetValuedMap<WikipediaLanguage, SimpleMisspelling> map1 = new HashSetValuedHashMap<>();
        map1.putAll(WikipediaLanguage.getDefault(), List.of(misspelling1, misspelling2));

        SimpleMisspelling misspelling3 = SimpleMisspelling.ofCaseInsensitive("B", "D");
        SetValuedMap<WikipediaLanguage, SimpleMisspelling> map2 = new HashSetValuedHashMap<>();
        map2.putAll(WikipediaLanguage.getDefault(), List.of(misspelling1, misspelling3));

        // Fake the update of the list in the manager
        obsoleteMisspellingListener.propertyChange(
            new PropertyChangeEvent(this, SimpleMisspellingLoader.PROPERTY_ITEMS, map1, map2)
        );

        verify(removeObsoleteReplacementType, never())
            .removeObsoleteReplacementTypes(eq(WikipediaLanguage.getDefault()), anyCollection());
    }
}
