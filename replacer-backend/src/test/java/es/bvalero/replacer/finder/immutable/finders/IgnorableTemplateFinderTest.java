package es.bvalero.replacer.finder.immutable.finders;

import es.bvalero.replacer.config.XmlConfiguration;
import es.bvalero.replacer.finder.immutable.Immutable;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = { IgnorableTemplateFinder.class, XmlConfiguration.class })
class IgnorableTemplateFinderTest {

    @Autowired
    private IgnorableTemplateFinder ignorableTemplateFinder;

    @Test
    void testFindIgnorableTemplate() {
        Assertions.assertFalse(ignorableTemplateFinder.findList("xxx #REDIRECCIÓN [[A]] yyy").isEmpty());
        Assertions.assertFalse(ignorableTemplateFinder.findList("xxx #redirección [[A]] yyy").isEmpty());
        Assertions.assertFalse(ignorableTemplateFinder.findList("xxx #REDIRECT [[A]] yyy").isEmpty());
        Assertions.assertTrue(ignorableTemplateFinder.findList("Otro contenido").isEmpty());
        Assertions.assertFalse(ignorableTemplateFinder.findList("xxx {{destruir|motivo}}").isEmpty());
        // Test it is not ignored by containing "{{pa}}
        Assertions.assertTrue(ignorableTemplateFinder.findList("Text {{Partial|Co-Director}} Text").isEmpty());

        // The finder returns the whole text
        String text = "xxx #REDIRECCIÓN [[A]] yyy";
        List<Immutable> results = ignorableTemplateFinder.findList(text);
        Assertions.assertFalse(results.isEmpty());
        Assertions.assertEquals(0, results.get(0).getStart());
        Assertions.assertEquals(text, results.get(0).getText());
    }
}
