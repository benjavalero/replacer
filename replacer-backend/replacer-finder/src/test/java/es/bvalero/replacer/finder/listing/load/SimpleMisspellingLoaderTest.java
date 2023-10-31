package es.bvalero.replacer.finder.listing.load;

import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.finder.listing.find.ListingFinder;
import es.bvalero.replacer.finder.listing.parse.SimpleMisspellingParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SimpleMisspellingLoaderTest {

    // Dependency injection
    private ListingFinder listingFinder;
    private SimpleMisspellingParser simpleMisspellingParser;

    private SimpleMisspellingLoader simpleMisspellingLoader;

    @BeforeEach
    public void setUp() {
        listingFinder = mock(ListingFinder.class);
        simpleMisspellingParser = mock(SimpleMisspellingParser.class);
        simpleMisspellingLoader = new SimpleMisspellingLoader(listingFinder, simpleMisspellingParser);
    }

    @Test
    void testLoad() throws ReplacerException {
        when(listingFinder.getSimpleMisspellingListing(any(WikipediaLanguage.class))).thenReturn("");

        simpleMisspellingLoader.load();

        verify(listingFinder).getSimpleMisspellingListing(WikipediaLanguage.SPANISH);
        verify(simpleMisspellingParser, atLeastOnce()).parseListing(anyString());
    }
}
