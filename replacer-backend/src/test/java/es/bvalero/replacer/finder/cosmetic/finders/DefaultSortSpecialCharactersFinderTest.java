package es.bvalero.replacer.finder.cosmetic.finders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.common.domain.Cosmetic;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

class DefaultSortSpecialCharactersFinderTest {

    @InjectMocks
    private DefaultSortSpecialCharactersFinder defaultSortSpecialCharactersFinder;

    @BeforeEach
    public void setUp() {
        defaultSortSpecialCharactersFinder = new DefaultSortSpecialCharactersFinder();
        MockitoAnnotations.openMocks(this);
    }

    @ParameterizedTest
    @CsvSource(
        value = {
            "{{ DEFAULTSORT : AES_Andes_2 }}, {{DEFAULTSORT:AES Andes 2}}",
            "{{ ORDENAR : AES_Andes_2 }}, {{ORDENAR:AES Andes 2}}",
        }
    )
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
