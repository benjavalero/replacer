package es.bvalero.replacer.page.save;

import static es.bvalero.replacer.page.save.PageSaveController.EMPTY_CONTENT;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.bvalero.replacer.common.domain.*;
import es.bvalero.replacer.common.dto.AccessTokenDto;
import es.bvalero.replacer.common.dto.PageReviewOptionsDto;
import es.bvalero.replacer.common.util.WikipediaDateUtils;
import es.bvalero.replacer.page.review.PageReviewOptions;
import es.bvalero.replacer.page.review.ReviewPage;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = PageSaveController.class)
class PageSaveControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PageSaveService pageSaveService;

    @Test
    void testSaveWithChanges() throws Exception {
        int pageId = 123;
        String title = "Q";
        String content = "X";
        LocalDateTime timestamp = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        String token = "A";
        String tokenSecret = "B";
        AccessToken accessToken = AccessToken.of(token, tokenSecret);
        PageSaveRequest request = new PageSaveRequest();
        ReviewPage reviewPage = new ReviewPage();
        reviewPage.setLang(WikipediaLanguage.SPANISH.getCode());
        reviewPage.setId(pageId);
        reviewPage.setTitle(title);
        reviewPage.setContent(content);
        reviewPage.setQueryTimestamp(WikipediaDateUtils.formatWikipediaTimestamp(timestamp));
        request.setPage(reviewPage);
        PageReviewOptionsDto search = new PageReviewOptionsDto();
        request.setOptions(search);
        request.setAccessToken(AccessTokenDto.fromDomain(accessToken));

        mvc
            .perform(
                post("/api/pages/123?lang=es&user=A")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isOk());

        WikipediaPage page = WikipediaPage
            .builder()
            .id(WikipediaPageId.of(WikipediaLanguage.SPANISH, pageId))
            .namespace(WikipediaNamespace.getDefault())
            .title(title)
            .content(content)
            .lastUpdate(timestamp)
            .queryTimestamp(timestamp)
            .build();
        verify(pageSaveService).savePageContent(page, null, PageReviewOptions.ofNoType(), accessToken);
    }

    @Test
    void testSaveWithNoChanges() throws Exception {
        int pageId = 123;
        String title = "Q";
        LocalDateTime timestamp = LocalDateTime.now();
        String token = "A";
        String tokenSecret = "B";
        AccessToken accessToken = AccessToken.of(token, tokenSecret);
        PageSaveRequest request = new PageSaveRequest();
        ReviewPage reviewPage = new ReviewPage();
        reviewPage.setLang(WikipediaLanguage.SPANISH.getCode());
        reviewPage.setId(pageId);
        reviewPage.setTitle(title);
        reviewPage.setContent(EMPTY_CONTENT);
        reviewPage.setQueryTimestamp(WikipediaDateUtils.formatWikipediaTimestamp(timestamp));
        request.setPage(reviewPage);
        PageReviewOptionsDto options = new PageReviewOptionsDto();
        request.setOptions(options);
        request.setAccessToken(AccessTokenDto.fromDomain(accessToken));

        mvc
            .perform(
                post("/api/pages/123?lang=es&user=A")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isOk());

        verify(pageSaveService).savePageWithNoChanges(pageId, PageReviewOptions.ofNoType());
    }
}
