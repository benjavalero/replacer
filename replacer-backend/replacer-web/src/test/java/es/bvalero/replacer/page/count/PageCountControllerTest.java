package es.bvalero.replacer.page.count;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import es.bvalero.replacer.WebMvcConfiguration;
import es.bvalero.replacer.common.domain.ResultCount;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.ReplacementKind;
import es.bvalero.replacer.finder.StandardType;
import es.bvalero.replacer.user.User;
import es.bvalero.replacer.user.WebUtils;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = { PageCountController.class, WebMvcConfiguration.class })
class PageCountControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private WebUtils webUtils;

    @MockitoBean
    private PageCountApi pageCountApi;

    @Test
    void testCountNotReviewedGroupedByType() throws Exception {
        User user = User.buildTestUser();
        when(webUtils.getAuthenticatedUser(any(HttpServletRequest.class))).thenReturn(user);

        StandardType type = StandardType.of(ReplacementKind.SIMPLE, "Y");
        ResultCount<StandardType> count = ResultCount.of(type, 100);
        Collection<ResultCount<StandardType>> counts = List.of(count);
        when(pageCountApi.countNotReviewedGroupedByType(user)).thenReturn(counts);

        mvc
            .perform(
                get("/api/page/type/count")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .cookie(WebUtils.buildAccessTokenCookie(user.getAccessToken()))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].k", is((int) ReplacementKind.SIMPLE.getCode())))
            .andExpect(jsonPath("$[0].l[0].s", is("Y")))
            .andExpect(jsonPath("$[0].l[0].c", is(100)));

        verify(pageCountApi).countNotReviewedGroupedByType(user);
    }
}
