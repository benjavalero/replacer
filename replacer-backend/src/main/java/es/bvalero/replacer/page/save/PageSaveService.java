package es.bvalero.replacer.page.save;

import es.bvalero.replacer.common.domain.AccessToken;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.page.review.PageReviewOptions;
import es.bvalero.replacer.page.review.PageReviewOptionsType;
import es.bvalero.replacer.repository.CustomModel;
import es.bvalero.replacer.repository.CustomRepository;
import es.bvalero.replacer.repository.ReplacementTypeRepository;
import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.WikipediaService;
import java.time.LocalDate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
class PageSaveService {

    private static final String EDIT_SUMMARY = "Reemplazos con [[Usuario:Benjavalero/Replacer|Replacer]]";
    private static final String COSMETIC_CHANGES = "mejoras cosméticas";

    @Autowired
    private ReplacementTypeRepository replacementTypeRepository;

    @Autowired
    private CustomRepository customRepository;

    @Autowired
    private WikipediaService wikipediaService;

    @Autowired
    private ApplyCosmeticsService applyCosmeticChanges;

    void savePageContent(
        WikipediaPage page,
        @Nullable Integer sectionId,
        PageReviewOptions options,
        AccessToken accessToken
    ) {
        // Apply cosmetic changes
        String textToSave = applyCosmeticChanges.applyCosmeticChanges(page);
        boolean applyCosmetics = !textToSave.equals(page.getContent());

        try {
            // Upload new content to Wikipedia
            wikipediaService.savePageContent(
                page.getId(),
                sectionId,
                textToSave,
                page.getQueryTimestamp(),
                buildEditSummary(options, applyCosmetics),
                accessToken
            );
        } catch (WikipediaException e) {
            LOGGER.error("Error saving page content", e);
        }

        // Mark page as reviewed in the database
        this.markAsReviewed(page.getId().getPageId(), options, true);
    }

    void savePageWithNoChanges(int pageId, PageReviewOptions options) {
        // Mark page as reviewed in the database
        this.markAsReviewed(pageId, options, false);
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

    private void markAsReviewed(int pageId, PageReviewOptions options, boolean updateDate) {
        String reviewer = options.getUser();
        switch (options.getOptionsType()) {
            case NO_TYPE:
                markAsReviewedNoType(pageId, options, reviewer, updateDate);
                break;
            case TYPE_SUBTYPE:
                markAsReviewedTypeSubtype(pageId, options, reviewer, updateDate);
                break;
            case CUSTOM:
                markAsReviewedCustom(pageId, options, reviewer);
                break;
        }
    }

    private void markAsReviewedNoType(int pageId, PageReviewOptions options, String reviewer, boolean updateDate) {
        replacementTypeRepository.updateReviewerByPageAndType(
            options.getWikipediaLanguage(),
            pageId,
            null,
            reviewer,
            updateDate
        );
    }

    private void markAsReviewedTypeSubtype(int pageId, PageReviewOptions options, String reviewer, boolean updateDate) {
        replacementTypeRepository.updateReviewerByPageAndType(
            options.getWikipediaLanguage(),
            pageId,
            options.getReplacementType(),
            reviewer,
            updateDate
        );
    }

    private void markAsReviewedCustom(int pageId, PageReviewOptions options, String reviewer) {
        // Custom replacements don't exist in the database to be reviewed
        String subtype = options.getSubtype();
        boolean cs = options.getCs() != null && Boolean.TRUE.equals(options.getCs());
        assert subtype != null;
        customRepository.addCustom(buildCustomReviewed(pageId, options.getWikipediaLanguage(), subtype, cs, reviewer));
    }

    private CustomModel buildCustomReviewed(
        int pageId,
        WikipediaLanguage lang,
        String replacement,
        boolean cs,
        String reviewer
    ) {
        return CustomModel
            .builder()
            .lang(lang.getCode())
            .pageId(pageId)
            .replacement(replacement)
            .cs((byte) (cs ? 1 : 0))
            .lastUpdate(LocalDate.now())
            .reviewer(reviewer)
            .build();
    }
}
