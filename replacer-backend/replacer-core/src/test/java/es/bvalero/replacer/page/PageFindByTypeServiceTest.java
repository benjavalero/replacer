package es.bvalero.replacer.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PageFindByTypeServiceTest {

    // Dependency injection
    private PageRepository pageRepository;

    private PageFindByTypeService pageFindByTypeService;

    @BeforeEach
    public void setUp() {
        pageRepository = mock(PageRepository.class);
        pageFindByTypeService = new PageFindByTypeService(pageRepository);
    }

    @Test
    void testFindPageTitlesToReviewByType() {
        // An array list to be able to sort
        List<String> list = Arrays.asList("Bo", "C", "Aá", "Bñ", null, "Ae");
        List<String> sorted = List.of("Aá", "Ae", "Bñ", "Bo", "C");

        WikipediaLanguage lang = WikipediaLanguage.getDefault();
        StandardType type = StandardType.DATE;
        when(pageRepository.findPageTitlesNotReviewedByType(lang, type)).thenReturn(list);

        Collection<String> result = pageFindByTypeService.findPagesToReviewByType(lang, type);
        assertEquals(sorted, result);

        verify(pageRepository).findPageTitlesNotReviewedByType(lang, type);
    }
}
