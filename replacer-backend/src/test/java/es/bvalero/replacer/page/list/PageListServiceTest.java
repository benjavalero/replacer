package es.bvalero.replacer.page.list;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.repository.PageRepository;
import es.bvalero.replacer.repository.ReplacementCountRepository;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class PageListServiceTest {

    @Mock
    private PageRepository pageRepository;

    @Mock
    private ReplacementCountRepository replacementCountRepository;

    @InjectMocks
    private PageListService pageListService;

    @BeforeEach
    public void setUp() {
        pageListService = new PageListService();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testFindPageTitlesToReviewByType() {
        // An array list to be able to sort
        List<String> list = Arrays.asList("Bo", "C", "Aá", "Bñ", null, "Ae");
        List<String> sorted = List.of("Aá", "Ae", "Bñ", "Bo", "C");

        when(pageRepository.findPageTitlesToReviewByType(WikipediaLanguage.getDefault(), "X", "Y")).thenReturn(list);

        Collection<String> result = pageListService.findPageTitlesToReviewByType(
            WikipediaLanguage.getDefault(),
            "X",
            "Y"
        );
        assertEquals(sorted, result);

        verify(pageRepository).findPageTitlesToReviewByType(WikipediaLanguage.getDefault(), "X", "Y");
    }

    @Test
    void testReviewAsSystemByType() {
        pageListService.reviewAsSystemByType(WikipediaLanguage.getDefault(), "X", "Y");

        verify(replacementCountRepository).reviewAsSystemByType(WikipediaLanguage.getDefault(), "X", "Y");
    }
}
