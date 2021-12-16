package es.bvalero.replacer.page.removeobsolete;

import static org.mockito.Mockito.verify;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.domain.WikipediaPageId;
import es.bvalero.replacer.repository.PageRepository;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class RemoveObsoletePageServiceTest {

    @Mock
    private PageRepository pageRepository;

    @InjectMocks
    private RemoveObsoletePageService removeObsoletePageService;

    @BeforeEach
    public void setUp() {
        removeObsoletePageService = new RemoveObsoletePageService();
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRemoveObsoletePages() {
        WikipediaPageId pageId = WikipediaPageId.of(WikipediaLanguage.getDefault(), 2);
        Collection<WikipediaPageId> pageIds = List.of(pageId);

        removeObsoletePageService.removeObsoletePages(pageIds);

        verify(pageRepository).removePagesById(pageIds);
    }
}
