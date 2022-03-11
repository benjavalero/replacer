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
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class SameLinkFinderTest {



    @InjectMocks
    private SameLinkFinder sameLinkFinder;

    @BeforeEach
    public void setUp() {
        sameLinkFinder = new SameLinkFinder();
        MockitoAnnotations.openMocks(this);
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
    void testSameLinkFinder(String text, String fix) {
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
