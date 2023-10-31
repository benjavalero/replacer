package es.bvalero.replacer.finder.listing.load;

import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.finder.listing.find.ListingFinder;
import es.bvalero.replacer.finder.listing.parse.FalsePositiveParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FalsePositiveLoaderTest {

    // Dependency injection
    private ListingFinder listingFinder;
    private FalsePositiveParser falsePositiveParser;

    private FalsePositiveLoader falsePositiveLoader;

    @BeforeEach
    public void setUp() {
        listingFinder = mock(ListingFinder.class);
        falsePositiveParser = mock(FalsePositiveParser.class);
        falsePositiveLoader = new FalsePositiveLoader(listingFinder, falsePositiveParser);
    }

    @Test
    void testLoad() throws ReplacerException {
        when(listingFinder.getFalsePositiveListing(any(WikipediaLanguage.class))).thenReturn("");

        falsePositiveLoader.load();

        verify(listingFinder).getFalsePositiveListing(WikipediaLanguage.SPANISH);
        verify(falsePositiveParser, atLeastOnce()).parseListing(anyString());
    }
}
