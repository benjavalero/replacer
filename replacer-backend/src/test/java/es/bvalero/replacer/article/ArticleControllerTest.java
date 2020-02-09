package es.bvalero.replacer.article;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.scribejava.core.model.OAuth1AccessToken;
import es.bvalero.replacer.authentication.AccessToken;
import es.bvalero.replacer.finder.CosmeticFindService;
import es.bvalero.replacer.finder.Suggestion;
import es.bvalero.replacer.replacement.ReplacementIndexService;
import es.bvalero.replacer.wikipedia.WikipediaService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = ArticleController.class)
public class ArticleControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ArticleReviewNoTypeService articleReviewNoTypeService;

    @MockBean
    private ArticleReviewTypeSubtypeService articleReviewTypeSubtypeService;

    @MockBean
    private ArticleReviewCustomService articleReviewCustomService;

    @MockBean
    private ReplacementIndexService replacementIndexService;

    @MockBean
    private WikipediaService wikipediaService;

    @MockBean
    private CosmeticFindService cosmeticFindService;

    @Test
    public void testFindRandomArticleWithReplacements() throws Exception {
        int id = 3;
        String title = "X";
        String content = "Y";
        Integer section = 2;
        String queryTimestamp = "Z";
        int start = 5;
        String rep = "A";
        Suggestion suggestion = Suggestion.of("a", "b");
        ArticleReplacement replacement = new ArticleReplacement(start, rep, Collections.singletonList(suggestion));
        List<ArticleReplacement> replacements = Collections.singletonList(replacement);
        ArticleReview review = new ArticleReview(id, title, content, section, queryTimestamp, replacements);
        when(articleReviewNoTypeService.findRandomArticleReview()).thenReturn(Optional.of(review));

        mvc.perform(get("/api/article/random")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id)))
                .andExpect(jsonPath("$.title", is(title)))
                .andExpect(jsonPath("$.content", is(content)))
                .andExpect(jsonPath("$.section", is(section)))
                .andExpect(jsonPath("$.queryTimestamp", is(queryTimestamp)))
                .andExpect(jsonPath("$.replacements[0].start", is(start)))
                .andExpect(jsonPath("$.replacements[0].text", is(rep)))
                .andExpect(jsonPath("$.replacements[0].suggestions[0].text", is("a")))
                .andExpect(jsonPath("$.replacements[0].suggestions[0].comment", is("b")));

        verify(articleReviewNoTypeService, times(1)).findRandomArticleReview();
    }

    @Test
    public void testFindRandomArticleByTypeAndSubtype() throws Exception {
        when(articleReviewTypeSubtypeService.findRandomArticleReview("X", "Y"))
                .thenReturn(Optional.of(new ArticleReview()));

        mvc.perform(get("/api/article/random/X/Y")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(articleReviewTypeSubtypeService, times(1))
                .findRandomArticleReview(eq("X"), eq("Y"));
    }

    @Test
    public void testFindRandomArticleByCustomReplacement() throws Exception {
        when(articleReviewCustomService.findRandomArticleReview("X", "Y"))
                .thenReturn(Optional.of(new ArticleReview()));

        mvc.perform(get("/api/article/random/Personalizado/X/Y")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(articleReviewCustomService, times(1))
                .findRandomArticleReview(eq("X"), eq("Y"));
    }

    @Test
    public void testFindArticleReviewById() throws Exception {
        when(articleReviewNoTypeService.getArticleReview(123))
                .thenReturn(Optional.of(new ArticleReview()));

        mvc.perform(get("/api/article/123")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(articleReviewNoTypeService, times(1))
                .getArticleReview(123);
    }

    @Test
    public void testFindArticleReviewByIdByTypeAndSubtype() throws Exception {
        when(articleReviewTypeSubtypeService.getArticleReview(123, "X", "Y"))
                .thenReturn(Optional.of(new ArticleReview()));

        mvc.perform(get("/api/article/123/X/Y")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(articleReviewTypeSubtypeService, times(1))
                .getArticleReview(123, "X", "Y");
    }

    @Test
    public void testFindArticleReviewByIdAndCustomReplacement() throws Exception {
        when(articleReviewCustomService.getArticleReview(123, "X", "Y"))
                .thenReturn(Optional.of(new ArticleReview()));

        mvc.perform(get("/api/article/123/Personalizado/X/Y")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(articleReviewCustomService, times(1))
                .getArticleReview(123, "X", "Y");
    }

    @Test
    public void testSaveWithChanges() throws Exception {
        int articleId = 123;
        int section = 3;
        String content = "X";
        String timestamp = "Y";
        String reviewer = "Z";
        AccessToken token = new AccessToken("A", "B");
        String type = "T";
        String subtype = "S";
        SaveArticle saveArticle = new SaveArticle(articleId, section, content, timestamp, reviewer, token, type, subtype);

        when(cosmeticFindService.applyCosmeticChanges(anyString())).thenReturn("C");

        mvc.perform(post("/api/article")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(saveArticle)))
                .andExpect(status().isOk());

        verify(cosmeticFindService, times(1)).applyCosmeticChanges(eq(content));
        verify(wikipediaService, times(1)).savePageContent(eq(articleId), eq("C"),
                eq(section), eq(timestamp), eq(new OAuth1AccessToken("A", "B")));
        verify(replacementIndexService, times(1)).reviewArticleReplacements(
                eq(articleId), eq(type), eq(subtype), eq(reviewer));
    }

    @Test
    public void testSaveWithNoChanges() throws Exception {
        int articleId = 123;
        int section = 3;
        String timestamp = "Y";
        String reviewer = "Z";
        AccessToken token = new AccessToken("A", "B");
        String type = "T";
        String subtype = "S";
        SaveArticle saveArticle = new SaveArticle(articleId, section, null, timestamp, reviewer, token, type, subtype);

        mvc.perform(post("/api/article")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(saveArticle)))
                .andExpect(status().isOk());

        verify(cosmeticFindService, times(0)).applyCosmeticChanges(anyString());
        verify(wikipediaService, times(0)).savePageContent(anyInt(), anyString(),
                anyInt(), anyString(), any(OAuth1AccessToken.class));
        verify(replacementIndexService, times(1)).reviewArticleReplacements(
                eq(articleId), eq(type), eq(subtype), eq(reviewer));
    }

}
