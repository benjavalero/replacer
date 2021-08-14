package es.bvalero.replacer.finder.listing.load;

import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.finder.listing.find.ListingFinder;
import es.bvalero.replacer.finder.listing.parse.SimpleMisspellingParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

class SimpleMisspellingLoaderTest {

    @Mock
    private ListingFinder listingFinder;

    @Mock
    private SimpleMisspellingParser simpleMisspellingParser;

    @InjectMocks
    private SimpleMisspellingLoader simpleMisspellingLoader;

    @BeforeEach
    public void setUp() {
        simpleMisspellingLoader = new SimpleMisspellingLoader();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testLoad() throws ReplacerException {
        Mockito.when(listingFinder.getSimpleMisspellingListing(Mockito.any(WikipediaLanguage.class))).thenReturn("");

        simpleMisspellingLoader.load();

        Mockito.verify(listingFinder).getSimpleMisspellingListing(WikipediaLanguage.SPANISH);
        Mockito.verify(simpleMisspellingParser, Mockito.atLeastOnce()).parseListing(Mockito.anyString());
    }
}
