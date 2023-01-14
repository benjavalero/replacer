package es.bvalero.replacer.finder.immutable.finders;

import static org.junit.jupiter.api.Assertions.*;

import es.bvalero.replacer.XmlConfiguration;
import es.bvalero.replacer.finder.Immutable;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = { RedirectionFinder.class, XmlConfiguration.class })
class RedirectionFinderTest {

    @Autowired
    private RedirectionFinder redirectionFinder;

    @Test
    void testFindIgnorableTemplate() {
        assertFalse(redirectionFinder.findList("xxx #REDIRECCIÓN [[A]] yyy").isEmpty());
        assertFalse(redirectionFinder.findList("xxx #redirección [[A]] yyy").isEmpty());
        assertFalse(redirectionFinder.findList("xxx #REDIRECT [[A]] yyy").isEmpty());
        assertTrue(redirectionFinder.findList("Otro contenido").isEmpty());
        assertTrue(redirectionFinder.findList("xxx {{destruir|motivo}}").isEmpty());

        // The finder returns the whole text
        String text = "xxx #REDIRECCIÓN [[A]] yyy";
        List<Immutable> results = redirectionFinder.findList(text);
        assertFalse(results.isEmpty());
        Assertions.assertEquals(0, results.get(0).getStart());
        Assertions.assertEquals(text, results.get(0).getText());
    }
}
