package es.bvalero.replacer.page;

import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.replacement.ReplacementService;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

class PageListServiceTest {

    @Mock
    private ReplacementService replacementService;

    @InjectMocks
    private PageListService pageListService;

    @BeforeEach
    public void setUp() {
        pageListService = new PageListService();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testFindPageList() {
        // An array list to be able to sort
        List<String> list = Arrays.asList("Bo", "C", "Aá", "Bñ", null, "Ae");
        List<String> sorted = List.of("Aá", "Ae", "Bñ", "Bo", "C");

        Mockito
            .when(
                replacementService.findPageTitlesToReviewBySubtype(
                    Mockito.any(WikipediaLanguage.class),
                    Mockito.anyString(),
                    Mockito.anyString()
                )
            )
            .thenReturn(list);

        List<String> result = pageListService.findPageTitlesToReviewBySubtype(WikipediaLanguage.SPANISH, "X", "Y");
        Assertions.assertEquals(sorted, result);
    }
}
