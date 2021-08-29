package es.bvalero.replacer.replacement;

import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.finder.listing.SimpleMisspelling;
import es.bvalero.replacer.finder.listing.load.SimpleMisspellingLoader;
import es.bvalero.replacer.finder.replacement.ReplacementType;
import java.beans.PropertyChangeEvent;
import java.util.*;
import org.apache.commons.collections4.SetValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

class ObsoleteMisspellingListenerTest {

    private static final SetValuedMap<WikipediaLanguage, String> EMPTY_MAP = new HashSetValuedHashMap<>();

    @Mock
    private ReplacementService replacementService;

    @InjectMocks
    private ObsoleteMisspellingListener obsoleteMisspellingListener;

    @BeforeEach
    public void setUp() {
        obsoleteMisspellingListener = new ObsoleteMisspellingListener();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testDeleteObsoleteMisspellings() {
        SimpleMisspelling misspelling1 = SimpleMisspelling.ofCaseInsensitive("A", "B");
        SimpleMisspelling misspelling2 = SimpleMisspelling.ofCaseInsensitive("B", "C");
        SetValuedMap<WikipediaLanguage, SimpleMisspelling> map1 = new HashSetValuedHashMap<>();
        map1.putAll(WikipediaLanguage.SPANISH, List.of(misspelling1, misspelling2));

        // Fake the update of the list in the manager
        obsoleteMisspellingListener.propertyChange(
            new PropertyChangeEvent(this, SimpleMisspellingLoader.PROPERTY_ITEMS, EMPTY_MAP, map1)
        );

        Mockito
            .verify(replacementService, Mockito.times(0))
            .deleteToBeReviewedBySubtype(Mockito.any(WikipediaLanguage.class), Mockito.anyString(), Mockito.anySet());

        SimpleMisspelling misspelling3 = SimpleMisspelling.ofCaseInsensitive("C", "D");
        SetValuedMap<WikipediaLanguage, SimpleMisspelling> map2 = new HashSetValuedHashMap<>();
        map2.putAll(WikipediaLanguage.SPANISH, List.of(misspelling2, misspelling3));

        // Fake the update of the list in the manager
        obsoleteMisspellingListener.propertyChange(
            new PropertyChangeEvent(this, SimpleMisspellingLoader.PROPERTY_ITEMS, map1, map2)
        );

        Mockito
            .verify(replacementService, Mockito.times(1))
            .deleteToBeReviewedBySubtype(
                WikipediaLanguage.SPANISH,
                ReplacementType.MISSPELLING_SIMPLE.getLabel(),
                Collections.singleton("A")
            );
    }
}