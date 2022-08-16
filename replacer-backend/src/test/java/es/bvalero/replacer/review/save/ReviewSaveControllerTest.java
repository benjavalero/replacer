package es.bvalero.replacer.review.save;

import static es.bvalero.replacer.review.save.ReviewSaveController.EMPTY_CONTENT;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.bvalero.replacer.common.domain.*;
import es.bvalero.replacer.common.domain.ReviewOptions;
import es.bvalero.replacer.common.dto.AccessTokenDto;
import es.bvalero.replacer.common.util.WikipediaDateUtils;
import es.bvalero.replacer.review.dto.*;
import es.bvalero.replacer.wikipedia.WikipediaConflictException;
import es.bvalero.replacer.wikipedia.WikipediaException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = ReviewSaveController.class)
class ReviewSaveControllerTest {

    private static final int pageId = 123;
    private static final String title = "Q";
    private static final String content = "X";
    private static final LocalDateTime timestamp = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    private static final AccessToken accessToken = AccessToken.of("A", "B");
    private static final WikipediaPage page = WikipediaPage
        .builder()
        .id(WikipediaPageId.of(WikipediaLanguage.SPANISH, pageId))
        .namespace(WikipediaNamespace.getDefault())
        .title(title)
        .content(content)
        .lastUpdate(timestamp)
        .queryTimestamp(timestamp)
        .build();
    ReviewedReplacement reviewed = ReviewedReplacement
        .builder()
        .pageId(page.getId())
        .type(ReplacementType.of(ReplacementKind.SIMPLE, "1"))
        .start(1)
        .reviewer("A")
        .build();

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReviewSaveService reviewSaveService;

    private SaveReviewRequest request;

    @BeforeEach
    public void setUp() {
        this.request = new SaveReviewRequest();
        ReviewPageDto reviewPage = new ReviewPageDto();
        reviewPage.setLang(WikipediaLanguage.SPANISH.getCode());
        reviewPage.setId(pageId);
        reviewPage.setTitle(title);
        reviewPage.setContent(content);
        reviewPage.setQueryTimestamp(WikipediaDateUtils.formatWikipediaTimestamp(timestamp));
        request.setPage(reviewPage);
        ReviewOptionsDto options = new ReviewOptionsDto();
        request.setOptions(options);
        ReviewedReplacementDto reviewedDto = new ReviewedReplacementDto();
        reviewedDto.setKind(reviewed.getType().getKind().getCode());
        reviewedDto.setSubtype(reviewed.getType().getSubtype());
        reviewedDto.setStart(reviewed.getStart());
        request.setReviewedReplacements(List.of(reviewedDto));
        request.setAccessToken(AccessTokenDto.fromDomain(accessToken));
    }

    @Test
    void testSaveWithChanges() throws Exception {
        mvc
            .perform(
                post("/api/review/123?lang=es&user=A")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isNoContent());

        verify(reviewSaveService).saveReviewContent(page, null, ReviewOptions.ofNoType(), accessToken);
        verify(reviewSaveService).markAsReviewed(List.of(reviewed), true);
    }

    @Test
    void testSaveWithNoChanges() throws Exception {
        request.getPage().setContent(EMPTY_CONTENT);

        mvc
            .perform(
                post("/api/review/123?lang=es&user=A")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isNoContent());

        verify(reviewSaveService, never())
            .saveReviewContent(any(WikipediaPage.class), anyInt(), any(ReviewOptions.class), any(AccessToken.class));
        verify(reviewSaveService).markAsReviewed(List.of(reviewed), false);
    }

    @Test
    void testPageIdMismatch() throws Exception {
        mvc
            .perform(
                post("/api/review/321?lang=es&user=A")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isBadRequest());

        verify(reviewSaveService, never()).markAsReviewed(anyCollection(), anyBoolean());
    }

    @Test
    void testPageLanguageMismatch() throws Exception {
        mvc
            .perform(
                post("/api/review/123?lang=en&user=A")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isBadRequest());

        verify(reviewSaveService, never()).markAsReviewed(anyCollection(), anyBoolean());
    }

    @Test
    void testPageNotValidEmptyContent() throws Exception {
        request.getPage().setContent("");

        mvc
            .perform(
                post("/api/review/123?lang=es&user=A")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isBadRequest());

        verify(reviewSaveService, never()).markAsReviewed(anyCollection(), anyBoolean());
    }

    @Test
    void testSaveWithChangesWithConflict() throws Exception {
        doThrow(WikipediaConflictException.class)
            .when(reviewSaveService)
            .saveReviewContent(page, null, ReviewOptions.ofNoType(), accessToken);

        mvc
            .perform(
                post("/api/review/123?lang=es&user=A")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isConflict());

        verify(reviewSaveService).saveReviewContent(page, null, ReviewOptions.ofNoType(), accessToken);
        verify(reviewSaveService, never()).markAsReviewed(anyCollection(), anyBoolean());
    }

    @Test
    void testSaveWithChangesNotAuthorizedWikipedia() throws Exception {
        doThrow(new WikipediaException("mwoauth-invalid-authorization"))
            .when(reviewSaveService)
            .saveReviewContent(page, null, ReviewOptions.ofNoType(), accessToken);

        mvc
            .perform(
                post("/api/review/123?lang=es&user=A")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isUnauthorized());

        verify(reviewSaveService).saveReviewContent(page, null, ReviewOptions.ofNoType(), accessToken);
        verify(reviewSaveService, never()).markAsReviewed(anyCollection(), anyBoolean());
    }

    @Test
    void testSaveWithChangesWikipediaException() throws Exception {
        doThrow(WikipediaException.class)
            .when(reviewSaveService)
            .saveReviewContent(page, null, ReviewOptions.ofNoType(), accessToken);

        mvc
            .perform(
                post("/api/review/123?lang=es&user=A")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isInternalServerError());

        verify(reviewSaveService).saveReviewContent(page, null, ReviewOptions.ofNoType(), accessToken);
        verify(reviewSaveService, never()).markAsReviewed(anyCollection(), anyBoolean());
    }
}
