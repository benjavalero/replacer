package es.bvalero.replacer.page.save;

import static org.junit.jupiter.api.Assertions.*;

import es.bvalero.replacer.JsonMapperConfiguration;
import es.bvalero.replacer.MediaWikiConfiguration;
import es.bvalero.replacer.common.domain.PageKey;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.page.find.WikipediaPageApiRepository;
import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import es.bvalero.replacer.wikipedia.WikipediaTimestamp;
import es.bvalero.replacer.wikipedia.api.WikipediaApiHelper;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
    classes = {
        WikipediaPageApiRepository.class,
        WikipediaPageSaveApiRepository.class,
        WikipediaApiHelper.class,
        MediaWikiConfiguration.class,
        JsonMapperConfiguration.class,
    }
)
class WikipediaPageSaveApiRepositoryIT {

    @Autowired
    private WikipediaPageApiRepository wikipediaPageRepository;

    @Autowired
    private WikipediaPageSaveApiRepository wikipediaPageSaveRepository;

    @Test
    void testGetEditToken() throws WikipediaException {
        // We pass on purpose a null access token to retrieve an anonymous edit token
        PageKey pageKey = PageKey.of(WikipediaLanguage.SPANISH, 6903884);
        EditToken editToken = wikipediaPageSaveRepository.getEditToken(pageKey, null);
        assertNotNull(editToken);
        assertTrue(editToken.getCsrfToken().endsWith("+\\"));
        assertNotNull(editToken.getTimestamp());
    }

    @Test
    void testSavePageWithConflicts() throws WikipediaException {
        WikipediaPage page = wikipediaPageRepository
            .findByTitle(WikipediaLanguage.SPANISH, "Wikipedia:Zona de pruebas/5")
            .orElseThrow(WikipediaException::new);

        String originalContent = page.getContent();
        String newContent = originalContent + "\nEdición sencilla para probar conflictos de edición.";
        String conflictContent = originalContent + "\nOtra edición sencilla para probar conflictos de edición.";

        // Save the new content
        WikipediaPageSaveCommand pageSave = WikipediaPageSaveCommand.builder()
            .pageKey(page.getPageKey())
            .content(newContent)
            .editSummary("Replacer Integration Test")
            .queryTimestamp(page.getQueryTimestamp())
            .build();
        // We pass on purpose a null access token to perform an anonymous edit
        wikipediaPageSaveRepository.save(pageSave, null);

        // Save the conflict content started 1 day before
        LocalDateTime before = page.getQueryTimestamp().toLocalDateTime().minusDays(1);
        WikipediaPageSaveCommand pageConflictSave = WikipediaPageSaveCommand.builder()
            .pageKey(page.getPageKey())
            .content(conflictContent)
            .editSummary("Replacer Integration Test")
            .queryTimestamp(WikipediaTimestamp.of(before))
            .build();

        // We pass on purpose a null access token to perform an anonymous edit
        assertThrows(WikipediaException.class, () -> wikipediaPageSaveRepository.save(pageConflictSave, null));
    }
}
