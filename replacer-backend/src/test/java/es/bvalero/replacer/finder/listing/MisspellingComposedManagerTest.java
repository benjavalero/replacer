package es.bvalero.replacer.finder.listing;

import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.finder.listing.find.ListingFinder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

class MisspellingComposedManagerTest {

    @Mock
    private ListingFinder listingFinder;

    @InjectMocks
    private MisspellingComposedManager misspellingComposedManager;

    @BeforeEach
    public void setUp() {
        misspellingComposedManager = new MisspellingComposedManager();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testUpdate() throws ReplacerException {
        Mockito.when(listingFinder.getComposedMisspellingListing(Mockito.any(WikipediaLanguage.class))).thenReturn("");

        misspellingComposedManager.scheduledItemListUpdate();

        Mockito.verify(listingFinder).getComposedMisspellingListing(WikipediaLanguage.SPANISH);
    }
}
