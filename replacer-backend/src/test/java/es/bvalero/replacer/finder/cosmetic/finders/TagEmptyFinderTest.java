package es.bvalero.replacer.finder.cosmetic.finders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.common.domain.Cosmetic;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class TagEmptyFinderTest {

    @InjectMocks
    private TagEmptyFinder tagEmptyFinder;

    @BeforeEach
    public void setUp() {
        tagEmptyFinder = new TagEmptyFinder();
        MockitoAnnotations.openMocks(this);
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "<span data-key=\"url\"></span>",
            "<div style=\"text-align: right; font-size: 85%;\"></div>",
            "<center></center>",
            "<ref></ref>",
        }
    )
    void testTagEmpty(String text) {
        List<Cosmetic> cosmetics = tagEmptyFinder.findList(text);

        assertEquals(1, cosmetics.size());
        assertEquals(text, cosmetics.get(0).getText());
        assertEquals("", cosmetics.get(0).getFix());
    }

    @ParameterizedTest
    @ValueSource(strings = { "<span>x</span>", "<div> </div>", "<ref name=\"x\"></ref>" })
    void testSingleSmallTag(String text) {
        List<Cosmetic> cosmetics = tagEmptyFinder.findList(text);

        assertTrue(cosmetics.isEmpty());
    }
}
