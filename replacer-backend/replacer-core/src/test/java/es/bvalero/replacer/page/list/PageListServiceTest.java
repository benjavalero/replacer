package es.bvalero.replacer.page.list;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.PageTitle;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.StandardType;
import es.bvalero.replacer.page.PageRepository;
import es.bvalero.replacer.page.PageSaveRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PageListServiceTest {

    // Dependency injection
    private PageRepository pageRepository;
    private PageSaveRepository pageSaveRepository;

    private PageListService pageListService;

    @BeforeEach
    public void setUp() {
        pageRepository = mock(PageRepository.class);
        pageSaveRepository = mock(PageSaveRepository.class);
        pageListService = new PageListService(pageRepository, pageSaveRepository);
    }

    @Test
    void testFindPageTitlesToReviewByType() {
        // An array list to be able to sort
        List<PageTitle> list = Stream.of("Bo", "C", "Aá", "Bñ", null, "Ae")
            .map(t -> t == null ? null : PageTitle.of(1, t))
            .collect(Collectors.toCollection(ArrayList::new));
        List<String> sorted = List.of("Aá", "Ae", "Bñ", "Bo", "C");

        WikipediaLanguage lang = WikipediaLanguage.getDefault();
        StandardType type = StandardType.DATE;
        when(pageRepository.findTitlesNotReviewedByType(lang, type)).thenReturn(list);

        Collection<String> result = pageListService.findPageTitlesNotReviewedByType(lang, type);
        assertEquals(sorted, result);

        verify(pageRepository).findTitlesNotReviewedByType(lang, type);
    }
}
