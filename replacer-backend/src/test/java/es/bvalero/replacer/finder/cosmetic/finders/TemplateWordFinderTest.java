package es.bvalero.replacer.finder.cosmetic.finders;

import static org.junit.jupiter.api.Assertions.assertEquals;

import es.bvalero.replacer.config.XmlConfiguration;
import es.bvalero.replacer.common.domain.Cosmetic;


import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("offline")
@SpringBootTest(classes = { TemplateWordFinder.class, XmlConfiguration.class})
class TemplateWordFinderTest {

    @Autowired
    private TemplateWordFinder templateWordFinder;

    @ParameterizedTest
    @CsvSource(value = { "{{Plantilla:Versalita|A}}, {{Versalita|A}}", "{{plantilla:DGRG}}, {{DGRG}}" })
    void testTemplateWordFinder(String text, String fix) {
        List<Cosmetic> cosmetics = templateWordFinder.findList(text);

        assertEquals(1, cosmetics.size());
        assertEquals(text, cosmetics.get(0).getText());
        assertEquals(fix, cosmetics.get(0).getFix());
    }
}
