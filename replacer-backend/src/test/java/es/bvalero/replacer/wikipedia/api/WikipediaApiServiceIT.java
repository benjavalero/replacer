package es.bvalero.replacer.wikipedia.api;

import static org.junit.jupiter.api.Assertions.*;

import es.bvalero.replacer.common.domain.*;
import es.bvalero.replacer.config.JsonMapperConfiguration;
import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.common.domain.WikipediaUser;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
    classes = {
        WikipediaPageApiRepository.class,
        WikipediaApiRequestHelper.class,
        MediaWikiApiConfiguration.class,
        JsonMapperConfiguration.class,
    }
)
class WikipediaApiServiceIT {

    @Autowired
    private WikipediaPageApiRepository wikipediaService;

    @Test
    void testGetPageContent() throws WikipediaException {
        String title = "Usuario:Benjavalero";
        WikipediaPage page = wikipediaService
            .findByTitle(WikipediaLanguage.SPANISH, title)
            .orElseThrow(WikipediaException::new);
        assertNotNull(page);
        assertEquals(6219990, page.getId().getPageId());
        assertEquals(title, page.getTitle());
        assertEquals(WikipediaNamespace.USER, page.getNamespace());
        assertTrue(page.getLastUpdate().getYear() >= 2016);
        assertTrue(page.getContent().contains("Orihuela"));
    }

    @Test
    void testGetPagesContent() throws WikipediaException {
        // We pass a null access token to retrieve an anonymous edit token
        Collection<WikipediaPage> pages = wikipediaService.findByIds(
            WikipediaLanguage.SPANISH,
            List.of(6219990, 6903884)
        );
        assertNotNull(pages);
        assertEquals(2, pages.size());
        assertTrue(pages.stream().anyMatch(page -> page.getId().getPageId() == 6219990));
        assertTrue(
            pages
                .stream()
                .filter(page -> page.getId().getPageId() == 6219990)
                .findAny()
                .orElseThrow(WikipediaException::new)
                .getContent()
                .contains("Orihuela")
        );
        assertTrue(pages.stream().anyMatch(page -> page.getId().getPageId() == 6903884));
        assertTrue(
            pages
                .stream()
                .filter(page -> page.getId().getPageId() == 6903884)
                .findAny()
                .orElseThrow(WikipediaException::new)
                .getContent()
                .contains("Pais Vasco")
        );
    }

    @Test
    void testGetPageContentUnavailable() {
        assertFalse(wikipediaService.findByTitle(WikipediaLanguage.SPANISH, "Usuario:Benjavaleroxx").isPresent());
    }

    @Test
    void testGetEditToken() throws WikipediaException {
        // We pass an empty access token to retrieve an anonymous edit token
        WikipediaPageId pageId = WikipediaPageId.of(WikipediaLanguage.SPANISH, 6903884);
        EditToken editToken = wikipediaService.getEditToken(pageId, AccessToken.empty());
        assertNotNull(editToken);
        assertTrue(editToken.getCsrfToken().endsWith("+\\"));
        assertNotNull(editToken.getTimestamp());
    }

    @Test
    void testSavePageWithConflicts() throws WikipediaException {
        WikipediaPage page = wikipediaService
            .findByTitle(WikipediaLanguage.SPANISH, "Wikipedia:Zona de pruebas/5")
            .orElseThrow(WikipediaException::new);

        String originalContent = page.getContent();
        String newContent = originalContent + "\nEdición sencilla para probar conflictos de edición.";
        String conflictContent = originalContent + "\nOtra edición sencilla para probar conflictos de edición.";

        // Save the new content
        wikipediaService.save(
            page.getId(),
            0,
            newContent,
            page.getQueryTimestamp(),
            "Replacer Integration Test",
            AccessToken.empty()
        );

        // Save the conflict content started 1 day before
        LocalDateTime before = page.getQueryTimestamp().minusDays(1);

        assertThrows(
            WikipediaException.class,
            () ->
                wikipediaService.save(
                    page.getId(),
                    0,
                    conflictContent,
                    before,
                    "Replacer Integration Test",
                    AccessToken.empty()
                )
        );
    }

    @Test
    void testGetUser() {
        String username = "Benjavalero";
        WikipediaUser user = wikipediaService.findByUsername(WikipediaLanguage.SPANISH, username).orElse(null);
        assertNotNull(user);
        assertEquals(WikipediaLanguage.SPANISH, user.getLang());
        assertEquals(username, user.getName());
        assertTrue(user.hasRights());
        assertFalse(user.isBot());
    }
}
