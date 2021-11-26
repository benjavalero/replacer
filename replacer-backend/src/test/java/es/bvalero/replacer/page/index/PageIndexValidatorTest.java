package es.bvalero.replacer.page.index;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.WikipediaNamespace;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.common.exception.ReplacerException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = { PageIndexValidator.class })
class PageIndexValidatorTest {

    @Autowired
    private PageIndexValidator pageIndexValidator;

    @Test
    void testIndexablePageIsIndexableByNamespace() throws ReplacerException {
        WikipediaPage notIndexable = mock(WikipediaPage.class);
        when(notIndexable.getNamespace()).thenReturn(WikipediaNamespace.WIKIPEDIA);
        WikipediaPage articlePage = mock(WikipediaPage.class);
        when(articlePage.getNamespace()).thenReturn(WikipediaNamespace.ARTICLE);
        WikipediaPage annexPage = mock(WikipediaPage.class);
        when(annexPage.getNamespace()).thenReturn(WikipediaNamespace.ANNEX);

        assertThrows(NonIndexablePageException.class, () -> pageIndexValidator.validateIndexable(notIndexable));
        pageIndexValidator.validateIndexable(articlePage);
        pageIndexValidator.validateIndexable(annexPage);
    }
}
