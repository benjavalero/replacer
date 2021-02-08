package es.bvalero.replacer.wikipedia;

import es.bvalero.replacer.ReplacerException;
import es.bvalero.replacer.config.XmlConfiguration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import javax.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = XmlConfiguration.class)
class WikipediaPageTest {

    @Resource
    private List<String> ignorableTemplates;

    @Test
    void testIsProcessableByNamespace() throws ReplacerException {
        Assertions.assertThrows(
            ReplacerException.class,
            () ->
                WikipediaPage.builder().namespace(WikipediaNamespace.WIKIPEDIA).build().validateProcessableByNamespace()
        );
        WikipediaPage.builder().namespace(WikipediaNamespace.ARTICLE).build().validateProcessableByNamespace();
        WikipediaPage.builder().namespace(WikipediaNamespace.ANNEX).build().validateProcessableByNamespace();
    }

    @Test
    void testIsProcessableByContent() throws ReplacerException {
        Assertions.assertThrows(
            ReplacerException.class,
            () ->
                WikipediaPage
                    .builder()
                    .content("xxx #REDIRECCIÓN [[A]] yyy")
                    .build()
                    .validateProcessableByContent(ignorableTemplates)
        );
        Assertions.assertThrows(
            ReplacerException.class,
            () ->
                WikipediaPage
                    .builder()
                    .content("xxx #redirección [[A]] yyy")
                    .build()
                    .validateProcessableByContent(ignorableTemplates)
        );
        Assertions.assertThrows(
            ReplacerException.class,
            () ->
                WikipediaPage
                    .builder()
                    .content("xxx #REDIRECT [[A]] yyy")
                    .build()
                    .validateProcessableByContent(ignorableTemplates)
        );
        WikipediaPage.builder().content("Otro contenido").build().validateProcessableByContent(ignorableTemplates);
        Assertions.assertThrows(
            ReplacerException.class,
            () ->
                WikipediaPage
                    .builder()
                    .content("xxx {{destruir|motivo}}")
                    .build()
                    .validateProcessableByContent(ignorableTemplates)
        );

        // Test it is not ignored by containing "{{pa}}
        WikipediaPage
            .builder()
            .content("Text {{Partial|Co-Director}} Text")
            .build()
            .validateProcessableByContent(ignorableTemplates);
    }

    @Test
    void testParseWikipediaDate() {
        LocalDate expected = LocalDate.of(2018, Month.AUGUST, 31);
        Assertions.assertEquals(expected, WikipediaUtils.parseWikipediaTimestamp("2018-08-31T05:17:28Z"));
    }

    @Test
    void testFormatWikipediaDate() {
        String expected = "2018-08-31T05:17:28Z";
        Assertions.assertEquals(
            expected,
            WikipediaUtils.formatWikipediaTimestamp(LocalDateTime.of(2018, Month.AUGUST, 31, 5, 17, 28))
        );
    }
}
