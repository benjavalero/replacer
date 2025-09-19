package es.bvalero.replacer.finder.immutable.finders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.finder.Immutable;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class XmlTagFinderTest {

    private XmlTagFinder xmlTagFinder;

    @BeforeEach
    public void setUp() {
        xmlTagFinder = new XmlTagFinder();
    }

    @ParameterizedTest
    @CsvSource(
        value = {
            "<span style=\"color:green;\">",
            "</span>",
            "<br />",
            "<hr style='margin-left:0;width:52px;background:#80BFFF'/>",
        }
    )
    void testXmlTagFinder(String text) {
        List<Immutable> matches = xmlTagFinder.findList(text);
        assertEquals(1, matches.size());

        assertEquals(0, matches.get(0).start());
        assertEquals(text, matches.get(0).text());
    }

    @ParameterizedTest
    @CsvSource(value = { "<!-- Esto es un comentario -->", "A not closed tag <br ", "A <false\npositive tag>" })
    void testXmlTagFinderNotMatched(String text) {
        List<Immutable> matches = xmlTagFinder.findList(text);

        assertTrue(matches.isEmpty());
    }
}
