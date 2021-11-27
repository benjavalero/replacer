package es.bvalero.replacer.page.index;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.WikipediaNamespace;
import es.bvalero.replacer.common.domain.WikipediaPage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = { PageIndexValidator.class })
class PageIndexValidatorTest {

    @Autowired
    private PageIndexValidator pageIndexValidator;

    @Test
    void testIsPageIndexableByNamespace() {
        WikipediaPage notIndexable = mock(WikipediaPage.class);
        when(notIndexable.getNamespace()).thenReturn(WikipediaNamespace.WIKIPEDIA);
        WikipediaPage articlePage = mock(WikipediaPage.class);
        when(articlePage.getNamespace()).thenReturn(WikipediaNamespace.ARTICLE);
        WikipediaPage annexPage = mock(WikipediaPage.class);
        when(annexPage.getNamespace()).thenReturn(WikipediaNamespace.ANNEX);

        assertFalse(pageIndexValidator.isPageIndexableByNamespace(notIndexable));
        assertTrue(pageIndexValidator.isPageIndexableByNamespace(articlePage));
        assertTrue(pageIndexValidator.isPageIndexableByNamespace(annexPage));
    }
    // TODO: Test validate by timestamp and by date
}
