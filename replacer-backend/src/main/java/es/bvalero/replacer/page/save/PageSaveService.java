package es.bvalero.replacer.page.save;

import es.bvalero.replacer.common.domain.AccessToken;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.domain.WikipediaPageId;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.cosmetic.CosmeticFinderService;
import es.bvalero.replacer.page.review.PageReviewMapper;
import es.bvalero.replacer.page.review.PageReviewOptions;
import es.bvalero.replacer.page.review.PageReviewSearch;
import es.bvalero.replacer.page.review.ReviewSection;
import es.bvalero.replacer.replacement.CustomEntity;
import es.bvalero.replacer.replacement.ReplacementService;
import es.bvalero.replacer.wikipedia.WikipediaDateUtils;
import es.bvalero.replacer.wikipedia.WikipediaService;
import java.time.LocalDate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
class PageSaveService {

    private static final String EDIT_SUMMARY = "Reemplazos con [[Usuario:Benjavalero/Replacer|Replacer]]";
    private static final String COSMETIC_CHANGES = "mejoras cosméticas";

    @Autowired
    private ReplacementService replacementService;

    @Autowired
    private WikipediaService wikipediaService;

    @Autowired
    private CosmeticFinderService cosmeticFinderService;

    void savePageContent(String user, PageSaveRequest request) throws ReplacerException {
        // FIXME: Not use the request DTO
        WikipediaLanguage lang = request.getPage().getLang();
        int pageId = request.getPage().getId();

        // Apply cosmetic changes
        FinderPage page = FinderPage.of(
            request.getPage().getLang(),
            request.getPage().getContent(),
            request.getPage().getTitle()
        );
        String textToSave = cosmeticFinderService.applyCosmeticChanges(page);
        boolean applyCosmetics = !textToSave.equals(page.getContent());
        ReviewSection section = request.getPage().getSection();

        // Upload new content to Wikipedia
        wikipediaService.savePageContent(
            WikipediaPageId.of(lang, pageId),
            section == null ? null : section.getId(),
            textToSave,
            WikipediaDateUtils.parseWikipediaTimestamp(request.getPage().getQueryTimestamp()),
            buildEditSummary(request.getSearch(), applyCosmetics),
            AccessToken.of(request.getToken(), request.getTokenSecret())
        );

        // Mark page as reviewed in the database
        this.markAsReviewed(pageId, lang, user, request.getSearch());
    }

    void savePageWithNoChanges(String user, PageSaveRequest request) {
        // FIXME: Not use the request DTO
        WikipediaLanguage lang = request.getPage().getLang();
        int pageId = request.getPage().getId();

        // Mark page as reviewed in the database
        this.markAsReviewed(pageId, lang, user, request.getSearch());
    }

    private String buildEditSummary(PageReviewSearch search, boolean applyCosmetics) {
        StringBuilder summary = new StringBuilder(EDIT_SUMMARY);
        if (StringUtils.isNotBlank(search.getType()) && StringUtils.isNotBlank(search.getSubtype())) {
            summary.append(": «").append(search.getSubtype()).append('»');
        }
        if (applyCosmetics) {
            summary.append(" + ").append(COSMETIC_CHANGES);
        }
        return summary.toString();
    }

    private void markAsReviewed(int pageId, WikipediaLanguage lang, String reviewer, PageReviewSearch search) {
        PageReviewOptions options = PageReviewMapper.fromDto(search, lang);
        switch (options.getOptionsType()) {
            case NO_TYPE:
                markAsReviewedNoType(pageId, options, reviewer);
                break;
            case TYPE_SUBTYPE:
                markAsReviewedTypeSubtype(pageId, options, reviewer);
                break;
            case CUSTOM:
                markAsReviewedCustom(pageId, options, reviewer);
                break;
        }
    }

    private void markAsReviewedNoType(int pageId, PageReviewOptions options, String reviewer) {
        replacementService.reviewByPageId(options.getLang(), pageId, null, null, reviewer);
    }

    private void markAsReviewedTypeSubtype(int pageId, PageReviewOptions options, String reviewer) {
        replacementService.reviewByPageId(options.getLang(), pageId, options.getType(), options.getSubtype(), reviewer);
    }

    private void markAsReviewedCustom(int pageId, PageReviewOptions options, String reviewer) {
        // Custom replacements don't exist in the database to be reviewed
        String subtype = options.getSubtype();
        boolean cs = options.getCs() != null && Boolean.TRUE.equals(options.getCs());
        assert subtype != null;
        replacementService.insert(buildCustomReviewed(pageId, options.getLang(), subtype, cs, reviewer));
    }

    private CustomEntity buildCustomReviewed(
        int pageId,
        WikipediaLanguage lang,
        String replacement,
        boolean cs,
        String reviewer
    ) {
        return CustomEntity
            .builder()
            .lang(lang.getCode())
            .pageId(pageId)
            .replacement(replacement)
            .cs(cs)
            .lastUpdate(LocalDate.now())
            .reviewer(reviewer)
            .build();
    }
}
