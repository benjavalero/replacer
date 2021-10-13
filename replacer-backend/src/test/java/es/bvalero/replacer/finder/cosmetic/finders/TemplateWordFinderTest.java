package es.bvalero.replacer.finder.cosmetic.finders;

import es.bvalero.replacer.config.XmlConfiguration;
import es.bvalero.replacer.finder.cosmetic.Cosmetic;
import es.bvalero.replacer.finder.cosmetic.checkwikipedia.CheckWikipediaOfflineService;
import es.bvalero.replacer.finder.cosmetic.checkwikipedia.CheckWikipediaService;
import es.bvalero.replacer.finder.immutable.finders.TemplateFinder;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("offline")
@SpringBootTest(
    classes = {
        TemplateWordFinder.class, XmlConfiguration.class, TemplateFinder.class, CheckWikipediaOfflineService.class,
    }
)
class TemplateWordFinderTest {

    @Autowired
    private CheckWikipediaService checkWikipediaService;

    @Autowired
    private TemplateFinder templateFinder;

    @Autowired
    private TemplateWordFinder templateWordFinder;

    @ParameterizedTest
    @CsvSource(
        value = {
            "{{Plantilla: Versalita|A}}, {{Versalita|A}}",
            "{{plantilla:DGRG}}, {{DGRG}}",
            "{{plantilla:{{#time:F}}}}, {{#time:F}}",
        }
    )
    void testTemplateWordFinder(String text, String fix) {
        List<Cosmetic> cosmetics = templateWordFinder.findList(text);

        Assertions.assertEquals(1, cosmetics.size());
        Assertions.assertEquals(text, cosmetics.get(0).getText());
        Assertions.assertEquals(fix, cosmetics.get(0).getFix());
    }
}
