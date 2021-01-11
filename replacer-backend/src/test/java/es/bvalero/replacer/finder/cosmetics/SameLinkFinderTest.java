package es.bvalero.replacer.finder.cosmetics;

import es.bvalero.replacer.finder.Cosmetic;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class SameLinkFinderTest {

    private final SameLinkFinder sameLinkFinder = new SameLinkFinder();

    @ParameterizedTest
    @CsvSource(value = { "[[test|test]], [[test]]", "[[Test|test]], [[test]]", "[[Test|Test]], [[Test]]" })
    void testSameLinkFinder(String text, String fix) {
        List<Cosmetic> cosmetics = sameLinkFinder.findList(text);

        Assertions.assertEquals(1, cosmetics.size());
        Assertions.assertEquals(text, cosmetics.get(0).getText());
        Assertions.assertEquals(fix, cosmetics.get(0).getFix());
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "[[test|Test]]",
            "[[Test|Mock]]",
            "[[Guerra polaco-soviética|Guerra Polaco-Soviética]]",
            "[[Uff Móvil|UFF móvil]]",
            "[[PetroChina|Petrochina]]",
        }
    )
    void testSameLinkNotValid(String text) {
        List<Cosmetic> cosmetics = sameLinkFinder.findList(text);

        Assertions.assertTrue(cosmetics.isEmpty());
    }
}
