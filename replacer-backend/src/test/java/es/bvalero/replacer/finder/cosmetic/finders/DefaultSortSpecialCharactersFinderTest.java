package es.bvalero.replacer.finder.cosmetic.finders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.finder.cosmetic.Cosmetic;
import es.bvalero.replacer.finder.cosmetic.checkwikipedia.CheckWikipediaService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class DefaultSortSpecialCharactersFinderTest {

    @Mock
    private CheckWikipediaService checkWikipediaService;

    @InjectMocks
    private DefaultSortSpecialCharactersFinder defaultSortSpecialCharactersFinder;

    @BeforeEach
    public void setUp() {
        defaultSortSpecialCharactersFinder = new DefaultSortSpecialCharactersFinder();
        MockitoAnnotations.initMocks(this);
    }

    @ParameterizedTest
    @CsvSource(value = { "{{DEFAULTSORT:AES_Andes}}, {{DEFAULTSORT:AES Andes}}" })
    void testSpecialCharacters(String text, String fix) {
        List<Cosmetic> cosmetics = defaultSortSpecialCharactersFinder.findList(text);

        assertEquals(1, cosmetics.size());
        assertEquals(text, cosmetics.get(0).getText());
        assertEquals(fix, cosmetics.get(0).getFix());
    }

    @ParameterizedTest
    @ValueSource(strings = { "{{DEFAULTSORT:AES Andes}}" })
    void testNoSpecialCharacters(String text) {
        List<Cosmetic> cosmetics = defaultSortSpecialCharactersFinder.findList(text);

        assertTrue(cosmetics.isEmpty());
    }
}
