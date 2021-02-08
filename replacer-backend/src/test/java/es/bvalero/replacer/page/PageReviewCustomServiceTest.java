package es.bvalero.replacer.page;

import es.bvalero.replacer.ReplacerException;
import es.bvalero.replacer.config.XmlConfiguration;
import es.bvalero.replacer.finder.replacement.Replacement;
import es.bvalero.replacer.finder.replacement.ReplacementFinderService;
import es.bvalero.replacer.replacement.ReplacementDao;
import es.bvalero.replacer.replacement.ReplacementEntity;
import es.bvalero.replacer.wikipedia.*;
import java.util.*;
import javax.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.modelmapper.ModelMapper;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = XmlConfiguration.class)
class PageReviewCustomServiceTest {

    private final int randomId = 1;
    private final int randomId2 = 2;
    private final String content = "XYZ";
    private final String content2 = "Y";
    private final WikipediaPage page = WikipediaPage
        .builder()
        .id(randomId)
        .lang(WikipediaLanguage.SPANISH)
        .namespace(WikipediaNamespace.ARTICLE)
        .content(content)
        .build();
    private final WikipediaPage page2 = WikipediaPage
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

    @Resource
    private List<String> ignorableTemplates;

    @Mock
    private ReplacementDao replacementDao;

    @Mock
    private WikipediaService wikipediaService;

    @Mock
    private ReplacementFinderService replacementFinderService;

    @Mock
    private SectionReviewService sectionReviewService;

    @Spy
    private ModelMapper modelMapper;

    @InjectMocks
    private PageReviewCustomService pageReviewCustomService;

    @BeforeEach
    public void setUp() {
        pageReviewCustomService = new PageReviewCustomService();
        pageReviewCustomService.setIgnorableTemplates(ignorableTemplates);
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testFindRandomPageToReviewCustom() throws ReplacerException {
        final String replacement = "R";
        final String suggestion = "S";

        // 1 result in Wikipedia
        List<Integer> pageIds = Collections.singletonList(randomId);
        Mockito
            .when(
                wikipediaService.getPageIdsByStringMatch(
                    Mockito.any(WikipediaLanguage.class),
                    Mockito.anyString(),
                    Mockito.anyBoolean(),
                    Mockito.anyInt(),
                    Mockito.anyInt()
                )
            )
            .thenReturn(PageSearchResult.of(1, pageIds));

        // The page exists in Wikipedia
        Mockito.when(wikipediaService.getPageById(randomId, WikipediaLanguage.SPANISH)).thenReturn(Optional.of(page));

        // The result is not already reviewed
        Mockito
            .when(replacementDao.findPageIdsReviewedByCustomTypeAndSubtype(WikipediaLanguage.SPANISH, replacement))
            .thenReturn(Collections.emptyList());

        // The page contains replacements
        Mockito
            .when(replacementFinderService.findCustomReplacements(page, replacement, suggestion))
            .thenReturn(replacements);

        PageReviewOptions options = PageReviewOptions.ofCustom(WikipediaLanguage.SPANISH, replacement, suggestion);
        Optional<PageReview> review = pageReviewCustomService.findRandomPageReview(options);

        Assertions.assertTrue(review.isPresent());
        Assertions.assertEquals(randomId, review.get().getId());
    }

    @Test
    void testFindRandomPageToReviewCustomNoResults() throws ReplacerException {
        final String replacement = "R";
        final String suggestion = "S";

        // 2 results in Wikipedia
        List<Integer> pageIds = new ArrayList<>(Arrays.asList(randomId, randomId2));
        Mockito
            .when(
                wikipediaService.getPageIdsByStringMatch(
                    Mockito.any(WikipediaLanguage.class),
                    Mockito.anyString(),
                    Mockito.anyBoolean(),
                    Mockito.anyInt(),
                    Mockito.anyInt()
                )
            )
            .thenReturn(PageSearchResult.of(pageIds.size(), pageIds))
            .thenReturn(PageSearchResult.ofEmpty());

        // The result 1 is already reviewed
        // The result 2 is not reviewed the first time, but reviewed the second time.
        Mockito
            .when(replacementDao.findPageIdsReviewedByCustomTypeAndSubtype(WikipediaLanguage.SPANISH, replacement))
            .thenReturn(Collections.singletonList(randomId));

        // The pages exist in Wikipedia
        Mockito.when(wikipediaService.getPageById(randomId2, WikipediaLanguage.SPANISH)).thenReturn(Optional.of(page2));

        // The page 2 contains no replacements
        Mockito
            .when(replacementFinderService.findCustomReplacements(page2, replacement, suggestion))
            .thenReturn(Collections.emptyList());

        PageReviewOptions options = PageReviewOptions.ofCustom(WikipediaLanguage.SPANISH, replacement, suggestion);
        Optional<PageReview> review = pageReviewCustomService.findRandomPageReview(options);

        Mockito
            .verify(wikipediaService, Mockito.times(2))
            .getPageIdsByStringMatch(
                Mockito.any(WikipediaLanguage.class),
                Mockito.anyString(),
                Mockito.anyBoolean(),
                Mockito.anyInt(),
                Mockito.anyInt()
            );

        // We add nothing to database
        Mockito.verify(replacementDao, Mockito.never()).insert(Mockito.any(ReplacementEntity.class));

        Assertions.assertFalse(review.isPresent());
    }
}
