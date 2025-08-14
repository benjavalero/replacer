package es.bvalero.replacer.page.list;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.StandardType;
import es.bvalero.replacer.page.PageRepository;
import es.bvalero.replacer.replacement.ReplacementSaveRepository;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PageListServiceTest {

    // Dependency injection
    private PageRepository pageRepository;
    private ReplacementSaveRepository replacementSaveRepository;

    private PageListService pageListService;

    @BeforeEach
    public void setUp() {
        pageRepository = mock(PageRepository.class);
        replacementSaveRepository = mock(ReplacementSaveRepository.class);
        pageListService = new PageListService(pageRepository, replacementSaveRepository);
    }

    @Test
    void testFindPageTitlesToReviewByType() {
        // An array list to be able to sort
        List<String> list = Arrays.asList("Bo", "C", "Aá", "Bñ", null, "Ae");
        List<String> sorted = List.of("Aá", "Ae", "Bñ", "Bo", "C");

        WikipediaLanguage lang = WikipediaLanguage.getDefault();
        StandardType type = StandardType.DATE;
        when(pageRepository.findTitlesNotReviewedByType(lang, type)).thenReturn(list);

        Collection<String> result = pageListService.findPageTitlesNotReviewedByType(lang, type);
        assertEquals(sorted, result);

        verify(pageRepository).findTitlesNotReviewedByType(lang, type);
    }
}
