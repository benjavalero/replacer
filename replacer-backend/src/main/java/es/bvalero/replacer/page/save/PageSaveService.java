package es.bvalero.replacer.page.save;

import es.bvalero.replacer.common.domain.*;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.cosmetic.CosmeticFinderService;
import es.bvalero.replacer.page.review.PageReviewOptions;
import es.bvalero.replacer.page.review.PageReviewOptionsType;
import es.bvalero.replacer.replacement.CustomEntity;
import es.bvalero.replacer.replacement.ReplacementService;
import es.bvalero.replacer.replacement.count.ReplacementCountRepository;
import es.bvalero.replacer.wikipedia.WikipediaService;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Service
class PageSaveService {

    private static final String EDIT_SUMMARY = "Reemplazos con [[Usuario:Benjavalero/Replacer|Replacer]]";
    private static final String COSMETIC_CHANGES = "mejoras cosméticas";

    @Autowired
    private ReplacementService replacementService;

    @Autowired
    private ReplacementCountRepository replacementCountRepository;

    @Autowired
    private WikipediaService wikipediaService;

    @Autowired
    private CosmeticFinderService cosmeticFinderService;

    void savePageContent(
        WikipediaPage page,
        @Nullable Integer sectionId,
        PageReviewOptions options,
        AccessToken accessToken
    ) throws ReplacerException {
        // Apply cosmetic changes
        FinderPage finderPage = FinderPage.of(page.getId().getLang(), page.getContent(), page.getTitle());
        String textToSave = cosmeticFinderService.applyCosmeticChanges(finderPage);
        boolean applyCosmetics = !textToSave.equals(finderPage.getContent());

        // Upload new content to Wikipedia
        wikipediaService.savePageContent(
            page.getId(),
            sectionId,
            textToSave,
            page.getQueryTimestamp(),
            buildEditSummary(options, applyCosmetics),
            accessToken
        );

        // Mark page as reviewed in the database
        this.markAsReviewed(page.getId().getPageId(), options);
    }

    void savePageWithNoChanges(int pageId, PageReviewOptions options) {
        // Mark page as reviewed in the database
        this.markAsReviewed(pageId, options);
    }

    private String buildEditSummary(PageReviewOptions options, boolean applyCosmetics) {
        StringBuilder summary = new StringBuilder(EDIT_SUMMARY);
        if (options.getOptionsType() == PageReviewOptionsType.TYPE_SUBTYPE) {
            summary.append(": «").append(options.getSubtype()).append('»');
        }
        if (applyCosmetics) {
            summary.append(" + ").append(COSMETIC_CHANGES);
        }
        return summary.toString();
    }

    private void markAsReviewed(int pageId, PageReviewOptions options) {
        String reviewer = options.getUser();
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
        replacementCountRepository.reviewByPageId(options.getLang(), pageId, null, null, reviewer);
    }

    private void markAsReviewedTypeSubtype(int pageId, PageReviewOptions options, String reviewer) {
        replacementCountRepository.reviewByPageId(
            options.getLang(),
            pageId,
            options.getType(),
            options.getSubtype(),
            reviewer
        );
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
