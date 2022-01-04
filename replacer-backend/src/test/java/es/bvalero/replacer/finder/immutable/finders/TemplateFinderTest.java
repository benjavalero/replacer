package es.bvalero.replacer.finder.immutable.finders;

import static org.junit.jupiter.api.Assertions.*;

import es.bvalero.replacer.config.XmlConfiguration;
import es.bvalero.replacer.finder.immutable.Immutable;
import es.bvalero.replacer.finder.listing.find.ListingOfflineFinder;
import es.bvalero.replacer.finder.listing.load.SimpleMisspellingLoader;
import es.bvalero.replacer.finder.listing.parse.SimpleMisspellingParser;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("offline")
@SpringBootTest(
    classes = {
        TemplateFinder.class,
        UppercaseFinder.class,
        SimpleMisspellingLoader.class,
        ListingOfflineFinder.class,
        SimpleMisspellingParser.class,
        XmlConfiguration.class,
    }
)
class TemplateFinderTest {

    @Autowired
    private SimpleMisspellingLoader simpleMisspellingLoader;

    @Autowired
    private TemplateFinder templateFinder;

    @Test
    void testFindTemplateNames() {
        String template1 = "{{Template1}}";
        String template2 = "{{Template2:Text2}}";
        String template3 = "{{Template3|param=value}}";
        String text = String.format("%s %s %s", template1, template2, template3);

        List<Immutable> matches = templateFinder.findList(text);

        List<String> names = List.of("Template1", "Template2", "Template3");
        assertTrue(matches.stream().map(Immutable::getText).collect(Collectors.toSet()).containsAll(names));
    }

    @Test
    void testFindCompleteTemplates() {
        String template1 = "{{Cita libro|param=value}}";
        String template2 = "{{#expr:Text2}}";
        String template3 = "{{Template3|param=value}}"; // Not captured
        String template4 = "{{IMDb nombre|1229738|Luis Posada}}";
        String template5 = "{{Enlace_roto|2=http://www.example.com}}";
        String text = String.format("%s %s %s %s %s", template1, template2, template3, template4, template5);

        List<Immutable> matches = templateFinder.findList(text);

        List<String> templates = List.of(template1, template2, template4, template5);
        assertTrue(matches.stream().map(Immutable::getText).collect(Collectors.toSet()).containsAll(templates));
        assertFalse(matches.stream().map(Immutable::getText).collect(Collectors.toSet()).contains(template3));
    }

    @Test
    void testFindParamValues() {
        String template1 = "{{Template1|param1= valor1}}"; // Only param
        String template2 = "{{Taxobox|param2=valor2}}"; // Ignored complete
        String template3 = "{{Template3|url= valor3 }}"; // Param + value
        String template4 = "{{Template4|param4= xxx.jpg }}"; // Param + value
        String template5 = "{{Fs player|nat=Brazil}}"; // Param + value
        String text = String.format("%s %s %s %s %s", template1, template2, template3, template4, template5);

        List<Immutable> matches = templateFinder.findList(text);

        List<String> params = List.of("param1", "url", "param4", "nat");
        List<String> values = List.of(" valor3 ", " xxx.jpg ", "Brazil");

        assertTrue(matches.stream().map(Immutable::getText).collect(Collectors.toSet()).containsAll(params));
        assertTrue(matches.stream().map(Immutable::getText).collect(Collectors.toSet()).containsAll(values));
        assertFalse(matches.stream().map(Immutable::getText).collect(Collectors.toSet()).contains("valor1"));
    }

    @Test
    void testCiteValue() {
        String text = "{{P|ps= «Libro Nº 34, año 1825, f. 145).»}}";

        List<Immutable> matches = templateFinder.findList(text);

        Set<String> expected = Set.of("P", "ps");
        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        assertEquals(expected, actual);
    }

    @Test
    void testValueWithComment() {
        String text = "{{Template|image = x.jpg <!-- A comment -->}}";

        List<Immutable> matches = templateFinder.findList(text);

        Set<String> expected = Set.of("Template", "image ", " x.jpg ");
        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        assertEquals(expected, actual);
    }

    @Test
    void testNestedTemplateValues() {
        String template2 = "{{Template2|url=value2}}";
        String template = String.format("{{Template1|param1=%s}}", template2);

        List<Immutable> matches = templateFinder.findList(template);

        Set<String> expected = Set.of("Template1", "Template2", "param1", "url", "value2");
        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        assertEquals(expected, actual);

        // Check positions
        assertEquals(2, matches.stream().filter(m -> m.getText().equals("Template1")).findAny().get().getStart());
        assertEquals(12, matches.stream().filter(m -> m.getText().equals("param1")).findAny().get().getStart());
        assertEquals(21, matches.stream().filter(m -> m.getText().equals("Template2")).findAny().get().getStart());
        assertEquals(31, matches.stream().filter(m -> m.getText().equals("url")).findAny().get().getStart());
        assertEquals(35, matches.stream().filter(m -> m.getText().equals("value2")).findAny().get().getStart());
    }

    @Test
    void testSpecialCharacters() {
        String text = "{{|||}}";

        List<Immutable> matches = templateFinder.findList(text);

        assertTrue(matches.isEmpty());
    }

    @Test
    void testContiguousTemplates() {
        String text = "{{T|x={{A}}{{B}} |y=C}}";

        List<Immutable> matches = templateFinder.findList(text);

        Set<String> expected = Set.of("T", "x", "A", "B", "y");
        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        assertEquals(expected, actual);
        assertTrue(matches.stream().allMatch(m -> m.getStart() >= 0));
    }

    @Test
    void testRepeatedParameters() {
        String text = "{{T|x = A|x = A}}";

        List<Immutable> matches = templateFinder.findList(text);

        // To calculate the parameter position we assume the parameters are not repeated in the template
        // Therefore in this case though we find both parameters always the first position is returned
        Set<Immutable> expected = Set.of(Immutable.of(2, "T"), Immutable.of(4, "x "));
        Set<Immutable> actual = new HashSet<>(matches);
        assertEquals(3, matches.size());
        assertEquals(expected, actual);
    }

    @Test
    void testSimilarParameters() {
        String text = "{{T|image_caption=A|image=A}}";

        List<Immutable> matches = templateFinder.findList(text);

        Set<Immutable> expected = Set.of(
            Immutable.of(2, "T"),
            Immutable.of(4, "image_caption"),
            Immutable.of(20, "image")
        );
        Set<Immutable> actual = new HashSet<>(matches);
        assertEquals(3, matches.size());
        assertEquals(expected, actual);
    }

    @Test
    void testValueWithLink() {
        String text = "{{T|x=[[A|B]]|y=C}}";

        List<Immutable> matches = templateFinder.findList(text);

        // To calculate the parameter position we assume the parameters are not repeated in the template
        // Therefore in this case though we find both parameters always the first position is returned
        Set<Immutable> expected = Set.of(Immutable.of(2, "T"), Immutable.of(4, "x"), Immutable.of(14, "y"));
        Set<Immutable> actual = new HashSet<>(matches);
        assertEquals(expected, actual);
    }

    @Test
    void testArgumentWithFile() {
        String text = "{{T|xxx.jpg}}";

        List<Immutable> matches = templateFinder.findList(text);

        Set<Immutable> expected = Set.of(Immutable.of(2, "T"), Immutable.of(4, "xxx.jpg"));
        Set<Immutable> actual = new HashSet<>(matches);
        assertEquals(expected, actual);
    }

    @Test
    void testArgumentWithUppercase() {
        // Load misspellings
        simpleMisspellingLoader.load();

        String text = "{{T|Enero de 1980|p=Febrero de 1979}}";

        List<Immutable> matches = templateFinder.findList(text);

        Set<Immutable> expected = Set.of(
            Immutable.of(2, "T"),
            Immutable.of(4, "Enero"),
            Immutable.of(18, "p"),
            Immutable.of(20, "Febrero")
        );
        Set<Immutable> actual = new HashSet<>(matches);
        assertEquals(expected, actual);
    }
}
