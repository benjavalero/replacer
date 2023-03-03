package es.bvalero.replacer.finder.listing.load;

import static org.junit.jupiter.api.Assertions.assertEquals;

import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.ObsoleteReplacementType;
import es.bvalero.replacer.finder.listing.Misspelling;
import es.bvalero.replacer.finder.listing.SimpleMisspelling;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections4.SetValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.junit.jupiter.api.Test;

class ObsoleteMisspellingListenerTest {

    private final ObsoleteMisspellingListener obsoleteMisspellingListener = new ObsoleteMisspellingListener();

    @Test
    void testGetObsoleteMisspellings() {
        SimpleMisspelling misspelling1 = SimpleMisspelling.ofCaseInsensitive("A", "B");
        SimpleMisspelling misspelling2 = SimpleMisspelling.ofCaseInsensitive("B", "C");
        SetValuedMap<WikipediaLanguage, Misspelling> map1 = new HashSetValuedHashMap<>();
        map1.putAll(WikipediaLanguage.getDefault(), List.of(misspelling1, misspelling2));

        SimpleMisspelling misspelling3 = SimpleMisspelling.ofCaseInsensitive("C", "D");
        SetValuedMap<WikipediaLanguage, Misspelling> map2 = new HashSetValuedHashMap<>();
        map2.putAll(WikipediaLanguage.getDefault(), List.of(misspelling2, misspelling3));

        Set<ObsoleteReplacementType> expected = Set.of(
            ObsoleteReplacementType.of(WikipediaLanguage.getDefault(), StandardType.of(ReplacementKind.SIMPLE, "A"))
        );
        assertEquals(expected, new HashSet<>(obsoleteMisspellingListener.getObsoleteMisspellings(map1, map2)));
    }
}
