package es.bvalero.replacer.finder.cosmetic.finders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.finder.Cosmetic;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class SameLinkFinderTest {

    private SameLinkFinder sameLinkFinder;

    @BeforeEach
    public void setUp() {
        sameLinkFinder = new SameLinkFinder();
    }

    @ParameterizedTest
    @CsvSource(
        value = {
            "[[test|test]], [[test]]",
            "[[Test|test]], [[test]]",
            "[[Test|Test]], [[Test]]",
            "[[IPhone|iPhone]], [[iPhone]]",
        }
    )
    void testSameLink(String text, String fix) {
        List<Cosmetic> cosmetics = sameLinkFinder.findList(text);

        assertEquals(1, cosmetics.size());
        assertEquals(text, cosmetics.get(0).getText());
        assertEquals(fix, cosmetics.get(0).getFix());
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "[[test|Test]]",
            "[[Test|Mock]]",
            "[[Guerra polaco-soviética|Guerra Polaco-Soviética]]",
            "[[Uff Móvil|UFF móvil]]",
            "[[PetroChina|Petrochina]]",
            "[[Sida|SIDA]]",
        }
    )
    void testSameLinkNotValid(String text) {
        List<Cosmetic> cosmetics = sameLinkFinder.findList(text);

        assertTrue(cosmetics.isEmpty());
    }
}
