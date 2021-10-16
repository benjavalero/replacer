package es.bvalero.replacer.finder.cosmetic.finders;

import static org.junit.jupiter.api.Assertions.assertEquals;

import es.bvalero.replacer.finder.cosmetic.Cosmetic;
import es.bvalero.replacer.finder.cosmetic.checkwikipedia.CheckWikipediaService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class HtmlDashFinderTest {

    @Mock
    private CheckWikipediaService checkWikipediaService;

    @InjectMocks
    private HtmlDashFinder htmlDashFinder;

    @BeforeEach
    public void setUp() {
        htmlDashFinder = new HtmlDashFinder();
        MockitoAnnotations.initMocks(this);
    }

    @ParameterizedTest
    @CsvSource(value = { "&mdash;, —", "&ndash;, –" })
    void testHtmlDashFinder(String text, String fix) {
        List<Cosmetic> cosmetics = htmlDashFinder.findList(text);

        assertEquals(1, cosmetics.size());
        assertEquals(text, cosmetics.get(0).getText());
        assertEquals(fix, cosmetics.get(0).getFix());
    }
}
