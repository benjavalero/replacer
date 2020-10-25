package es.bvalero.replacer.page;

import es.bvalero.replacer.replacement.ReplacementDao;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import java.util.ArrayList;
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
    private ReplacementDao replacementDao;

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
        List<String> list = new ArrayList<>(List.of("Bo", "C", "Aá", "Bñ", "Ae"));
        List<String> sorted = List.of("Aá", "Ae", "Bñ", "Bo", "C");

        Mockito
            .when(
                replacementDao.findPageTitlesByTypeAndSubtype(
                    Mockito.any(WikipediaLanguage.class),
                    Mockito.anyString(),
                    Mockito.anyString()
                )
            )
            .thenReturn(list);

        List<String> result = pageListService.findPageList(WikipediaLanguage.SPANISH, "X", "Y");
        Assertions.assertEquals(sorted, result);
    }
}
