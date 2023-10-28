package es.bvalero.replacer.finder.cosmetic.finders;

import static org.junit.jupiter.api.Assertions.assertEquals;

import es.bvalero.replacer.FinderProperties;
import es.bvalero.replacer.finder.Cosmetic;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("offline")
@EnableConfigurationProperties(FinderProperties.class)
@SpringBootTest(classes = TemplateWordFinder.class)
class TemplateWordFinderTest {

    @Autowired
    private TemplateWordFinder templateWordFinder;

    @ParameterizedTest
    @CsvSource(value = { "{{Plantilla:Versalita|A}}, {{Versalita|A}}", "{{plantilla:DGRG}}, {{DGRG}}" })
    void testTemplateWord(String text, String fix) {
        List<Cosmetic> cosmetics = templateWordFinder.findList(text);

        assertEquals(1, cosmetics.size());
        assertEquals(text, cosmetics.get(0).getText());
        assertEquals(fix, cosmetics.get(0).getFix());
    }
}
