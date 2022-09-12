package es.bvalero.replacer.page.list;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.repository.PageRepository;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class PageFindByTypeServiceTest {

    @Mock
    private PageRepository pageRepository;

    @InjectMocks
    private PageFindByTypeService pageFindByTypeService;

    @BeforeEach
    public void setUp() {
        pageFindByTypeService = new PageFindByTypeService();
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindPageTitlesToReviewByType() {
        // An array list to be able to sort
        List<String> list = Arrays.asList("Bo", "C", "Aá", "Bñ", null, "Ae");
        List<String> sorted = List.of("Aá", "Ae", "Bñ", "Bo", "C");

        ReplacementType type = ReplacementType.of(ReplacementKind.STYLE, "Y");
        when(pageRepository.findPageTitlesToReviewByType(WikipediaLanguage.getDefault(), type)).thenReturn(list);

        Collection<String> result = pageFindByTypeService.findPagesToReviewByType(WikipediaLanguage.getDefault(), type);
        assertEquals(sorted, result);

        verify(pageRepository).findPageTitlesToReviewByType(WikipediaLanguage.getDefault(), type);
    }
}
