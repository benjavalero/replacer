package es.bvalero.replacer.article;

import es.bvalero.replacer.ReplacerException;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.ReplacementFindService;
import es.bvalero.replacer.replacement.ReplacementIndexService;
import es.bvalero.replacer.replacement.ReplacementRepository;
import es.bvalero.replacer.wikipedia.*;
import java.util.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.modelmapper.ModelMapper;

public class ArticleReviewCustomServiceTest {
    private final int randomId = 1;
    private final int randomId2 = 2;
    private final String content = "XYZ";
    private final String content2 = "Y";
    private final WikipediaPage article = WikipediaPage
        .builder()
        .id(randomId)
        .lang(WikipediaLanguage.SPANISH)
        .namespace(WikipediaNamespace.ARTICLE)
        .content(content)
        .build();
    private final WikipediaPage article2 = WikipediaPage
        .builder()
        .id(randomId2)
        .lang(WikipediaLanguage.SPANISH)
        .namespace(WikipediaNamespace.ANNEX)
        .content(content2)
        .build();
    private final int offset = 1;
    private final Replacement replacement = Replacement
        .builder()
        .start(offset)
        .type("X")
        .subtype("Y")
        .text("Y")
        .build();
    private final List<Replacement> replacements = Collections.singletonList(replacement);

    @Mock
    private ReplacementRepository replacementRepository;

    @Mock
    private WikipediaService wikipediaService;

    @Mock
    private ReplacementFindService replacementFindService;

    @Mock
    private ReplacementIndexService replacementIndexService;

    @Mock
    private SectionReviewService sectionReviewService;

    @Spy
    private ModelMapper modelMapper;

    @InjectMocks
    private ArticleReviewCustomService articleService;

    @BeforeEach
    public void setUp() {
        articleService = new ArticleReviewCustomService();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testFindRandomArticleToReviewCustom() throws ReplacerException {
        final String replacement = "R";
        final String suggestion = "S";

        // 1 result in Wikipedia
        Mockito
            .when(wikipediaService.getPageIdsByStringMatch(Mockito.anyString(), Mockito.any(WikipediaLanguage.class)))
            .thenReturn(Collections.singleton(randomId));

        // The article exists in Wikipedia
        Mockito
            .when(wikipediaService.getPageById(randomId, WikipediaLanguage.SPANISH))
            .thenReturn(Optional.of(article));

        // The result is not already reviewed
        Mockito
            .when(
                replacementRepository.countByArticleIdAndLangAndTypeAndSubtypeAndReviewerNotNull(
                    randomId,
                    WikipediaLanguage.SPANISH.getCode(),
                    ReplacementFindService.CUSTOM_FINDER_TYPE,
                    replacement
                )
            )
            .thenReturn(0L);

        // The article contains replacements
        Mockito
            .when(
                replacementFindService.findCustomReplacements(
                    content,
                    replacement,
                    suggestion,
                    WikipediaLanguage.SPANISH
                )
            )
            .thenReturn(replacements);

        ArticleReviewOptions options = ArticleReviewOptions.ofCustom(
            WikipediaLanguage.SPANISH,
            replacement,
            suggestion
        );
        Optional<ArticleReview> review = articleService.findRandomArticleReview(options);

        Assertions.assertTrue(review.isPresent());
        Assertions.assertEquals(randomId, review.get().getId());
    }

    @Test
    public void testFindRandomArticleToReviewCustomNoResults() throws ReplacerException {
        final String replacement = "R";
        final String suggestion = "S";

        // 2 results in Wikipedia
        Mockito
            .when(wikipediaService.getPageIdsByStringMatch(Mockito.anyString(), Mockito.any(WikipediaLanguage.class)))
            .thenReturn(new HashSet<>(Arrays.asList(randomId, randomId2)));

        // The result 1 is already reviewed
        // The result 2 is not reviewed the first time, but reviewed the second time.
        Mockito
            .when(
                replacementRepository.countByArticleIdAndLangAndTypeAndSubtypeAndReviewerNotNull(
                    randomId,
                    WikipediaLanguage.SPANISH.getCode(),
                    ReplacementFindService.CUSTOM_FINDER_TYPE,
                    replacement
                )
            )
            .thenReturn(1L);
        Mockito
            .when(
                replacementRepository.countByArticleIdAndLangAndTypeAndSubtypeAndReviewerNotNull(
                    randomId2,
                    WikipediaLanguage.SPANISH.getCode(),
                    ReplacementFindService.CUSTOM_FINDER_TYPE,
                    replacement
                )
            )
            .thenReturn(0L)
            .thenReturn(1L);

        // The articles exist in Wikipedia
        Mockito
            .when(wikipediaService.getPageById(randomId, WikipediaLanguage.SPANISH))
            .thenReturn(Optional.of(article));
        Mockito
            .when(wikipediaService.getPageById(randomId2, WikipediaLanguage.SPANISH))
            .thenReturn(Optional.of(article2));

        // The article 2 contains no replacements
        Mockito
            .when(
                replacementFindService.findCustomReplacements(
                    content2,
                    replacement,
                    suggestion,
                    WikipediaLanguage.SPANISH
                )
            )
            .thenReturn(Collections.emptyList());

        ArticleReviewOptions options = ArticleReviewOptions.ofCustom(
            WikipediaLanguage.SPANISH,
            replacement,
            suggestion
        );
        Optional<ArticleReview> review = articleService.findRandomArticleReview(options);

        Mockito
            .verify(replacementIndexService, Mockito.times(1))
            .addCustomReviewedReplacement(Mockito.anyInt(), Mockito.any(WikipediaLanguage.class), Mockito.anyString());

        Assertions.assertFalse(review.isPresent());
    }
}
