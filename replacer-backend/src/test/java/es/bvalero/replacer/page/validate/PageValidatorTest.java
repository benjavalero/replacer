package es.bvalero.replacer.page.validate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.WikipediaNamespace;
import es.bvalero.replacer.common.exception.ReplacerException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = { PageValidator.class })
class PageValidatorTest {

    @Autowired
    private PageValidator pageValidator;

    @Test
    void testIndexablePageIsProcessableByNamespace() throws ReplacerException {
        ValidatePage notProcessable = mock(ValidatePage.class);
        when(notProcessable.getNamespace()).thenReturn(WikipediaNamespace.WIKIPEDIA);
        ValidatePage articlePage = mock(ValidatePage.class);
        when(articlePage.getNamespace()).thenReturn(WikipediaNamespace.ARTICLE);
        ValidatePage annexPage = mock(ValidatePage.class);
        when(annexPage.getNamespace()).thenReturn(WikipediaNamespace.ANNEX);

        assertThrows(ReplacerException.class, () -> pageValidator.validateProcessable(notProcessable));
        pageValidator.validateProcessable(articlePage);
        pageValidator.validateProcessable(annexPage);
    }
}
