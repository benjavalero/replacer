package es.bvalero.replacer.finder.listing.load;

import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.finder.listing.find.ListingFinder;
import es.bvalero.replacer.finder.listing.parse.FalsePositiveParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

class FalsePositiveLoaderTest {

    @Mock
    private ListingFinder listingFinder;

    @Mock
    private FalsePositiveParser falsePositiveParser;

    @InjectMocks
    private FalsePositiveLoader falsePositiveLoader;

    @BeforeEach
    public void setUp() {
        falsePositiveLoader = new FalsePositiveLoader();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testLoad() throws ReplacerException {
        Mockito.when(listingFinder.getFalsePositiveListing(Mockito.any(WikipediaLanguage.class))).thenReturn("");

        falsePositiveLoader.load();

        Mockito.verify(listingFinder).getFalsePositiveListing(WikipediaLanguage.SPANISH);
        Mockito.verify(falsePositiveParser, Mockito.atLeastOnce()).parseListing(Mockito.anyString());
    }
}
