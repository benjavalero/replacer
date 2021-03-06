package es.bvalero.replacer.finder.immutable;

import es.bvalero.replacer.config.XmlConfiguration;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = { CompleteTagFinder.class, XmlConfiguration.class })
class CompleteTagFinderTest {

    @Autowired
    private CompleteTagFinder completeTagFinder;

    @ParameterizedTest
    @ValueSource(
        strings = {
            "<math class=\"latex\">An <i>example</i>\n in LaTeX</math>",
            "<source>Another example</source>",
            "<ref>Text</ref>",
        }
    )
    void testFindCompleteTag(String text) {
        List<Immutable> matches = completeTagFinder.findList(text);

        Assertions.assertEquals(1, matches.size());
        Assertions.assertEquals(text, matches.get(0).getText());
    }

    @ParameterizedTest
    @ValueSource(strings = { "<ref name=NH05/>", "<ref>Unclosed tag", "<br>", "</p>", "<unknown>Unknown</unknown>" })
    void testFindCompleteTagNonValid(String text) {
        List<Immutable> matches = completeTagFinder.findList(text);

        Assertions.assertTrue(matches.isEmpty());
    }

    @Test
    void testDoubleTags() {
        String text = "A <ref>A</ref> and <ref>B</ref>.";

        List<Immutable> matches = completeTagFinder.findList(text);

        Assertions.assertEquals(2, matches.size());
    }
}
