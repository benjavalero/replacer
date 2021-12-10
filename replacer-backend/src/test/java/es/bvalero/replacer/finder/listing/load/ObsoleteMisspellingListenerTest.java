package es.bvalero.replacer.finder.listing.load;

import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.listing.SimpleMisspelling;
import java.beans.PropertyChangeEvent;
import java.util.Collections;
import java.util.List;
import org.apache.commons.collections4.SetValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ObsoleteMisspellingListenerTest {

    private static final SetValuedMap<WikipediaLanguage, String> EMPTY_MAP = new HashSetValuedHashMap<>();

    @Test
    void testDeleteObsoleteMisspellings() {
        ObsoleteMisspellingListener obsoleteMisspellingListener = Mockito.spy(ObsoleteMisspellingListener.class);

        SimpleMisspelling misspelling1 = SimpleMisspelling.ofCaseInsensitive("A", "B");
        SimpleMisspelling misspelling2 = SimpleMisspelling.ofCaseInsensitive("B", "C");
        SetValuedMap<WikipediaLanguage, SimpleMisspelling> map1 = new HashSetValuedHashMap<>();
        map1.putAll(WikipediaLanguage.SPANISH, List.of(misspelling1, misspelling2));

        // Fake the update of the list in the manager
        obsoleteMisspellingListener.propertyChange(
            new PropertyChangeEvent(this, SimpleMisspellingLoader.PROPERTY_ITEMS, EMPTY_MAP, map1)
        );

        verify(obsoleteMisspellingListener, times(0))
            .processObsoleteReplacementTypes(any(WikipediaLanguage.class), any(ReplacementKind.class), anySet());

        SimpleMisspelling misspelling3 = SimpleMisspelling.ofCaseInsensitive("C", "D");
        SetValuedMap<WikipediaLanguage, SimpleMisspelling> map2 = new HashSetValuedHashMap<>();
        map2.putAll(WikipediaLanguage.SPANISH, List.of(misspelling2, misspelling3));

        // Fake the update of the list in the manager
        obsoleteMisspellingListener.propertyChange(
            new PropertyChangeEvent(this, SimpleMisspellingLoader.PROPERTY_ITEMS, map1, map2)
        );

        verify(obsoleteMisspellingListener)
            .processObsoleteReplacementTypes(
                WikipediaLanguage.SPANISH,
                ReplacementKind.MISSPELLING_SIMPLE,
                Collections.singleton("A")
            );
    }
}
