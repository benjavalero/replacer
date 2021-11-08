package es.bvalero.replacer.page.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import es.bvalero.replacer.domain.WikipediaLanguage;
import es.bvalero.replacer.replacement.ReplacementService;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class IndexablePageCacheRepositoryTest {

    @Mock
    private IndexablePageRepository indexablePageRepository;

    @Mock
    private ReplacementService replacementService;

    @InjectMocks
    private IndexablePageCacheRepository indexablePageCacheRepository;

    @BeforeEach
    public void setUp() {
        indexablePageCacheRepository = new IndexablePageCacheRepository();
        indexablePageCacheRepository.setChunkSize(1000);
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testFindDatabaseReplacements() {
        // In DB: replacements for page 2 (first load) and 1001 (second load)
        // We ask for the page 1 and 1001, so the page 2 will be cleaned.
        IndexableReplacementDB replacement1 = IndexableReplacementDB
            .builder()
            .type("")
            .subtype("")
            .position(0)
            .context("")
            .lastUpdate(LocalDate.now())
            .build();
        IndexablePageDB page1 = IndexablePageDB
            .builder()
            .lang(WikipediaLanguage.SPANISH)
            .id(2)
            .replacements(List.of(replacement1))
            .build();
        IndexableReplacementDB replacement2 = IndexableReplacementDB
            .builder()
            .type("")
            .subtype("")
            .position(0)
            .context("")
            .lastUpdate(LocalDate.now())
            .build();
        IndexablePageDB page2 = IndexablePageDB
            .builder()
            .lang(WikipediaLanguage.SPANISH)
            .id(1001)
            .replacements(List.of(replacement2))
            .build();
        when(indexablePageRepository.findByPageIdInterval(WikipediaLanguage.SPANISH, 1, 1000))
            .thenReturn(List.of(page1));
        when(indexablePageRepository.findByPageIdInterval(WikipediaLanguage.SPANISH, 1001, 2000))
            .thenReturn(List.of(page2));

        Optional<IndexablePageDB> indexablePageDB = indexablePageCacheRepository.findByPageId(
            WikipediaLanguage.SPANISH,
            1
        );
        assertTrue(indexablePageDB.isEmpty());

        indexablePageDB = indexablePageCacheRepository.findByPageId(WikipediaLanguage.SPANISH, 1001);
        assertEquals(page2, indexablePageDB.orElse(null));

        // Check that the page 2 has been cleaned
        verify(replacementService).indexObsoleteByPageId(WikipediaLanguage.SPANISH, 2);
    }

    @Test
    void testFindDatabaseReplacementsWithEmptyLoad() {
        // In DB: replacement for page 1001
        // So the first load is enlarged
        IndexableReplacementDB replacement = IndexableReplacementDB
            .builder()
            .type("")
            .subtype("")
            .position(0)
            .context("")
            .lastUpdate(LocalDate.now())
            .build();
        IndexablePageDB page = IndexablePageDB
            .builder()
            .lang(WikipediaLanguage.SPANISH)
            .id(1001)
            .replacements(List.of(replacement))
            .build();
        when(indexablePageRepository.findByPageIdInterval(WikipediaLanguage.SPANISH, 1, 2000))
            .thenReturn(List.of(page));

        Optional<IndexablePageDB> indexablePageDB = indexablePageCacheRepository.findByPageId(
            WikipediaLanguage.SPANISH,
            1001
        );
        assertEquals(page, indexablePageDB.orElse(null));
    }
}
