package es.bvalero.replacer.review.save;

import es.bvalero.replacer.common.domain.AccessToken;
import es.bvalero.replacer.common.domain.ReviewOptions;
import es.bvalero.replacer.common.domain.ReviewOptionsType;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.common.domain.WikipediaPageId;
import es.bvalero.replacer.repository.CustomRepository;
import es.bvalero.replacer.repository.PageRepository;
import es.bvalero.replacer.repository.ReplacementTypeRepository;
import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.WikipediaPageRepository;
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
    private PageRepository pageRepository;

    @Autowired
    private ReplacementTypeRepository replacementTypeRepository;

    @Autowired
    private CustomRepository customRepository;

    @Autowired
    private WikipediaPageRepository wikipediaPageRepository;

    @Autowired
    private ApplyCosmeticsService applyCosmeticChanges;

    void savePageContent(
        WikipediaPage page,
        @Nullable Integer sectionId,
        ReviewOptions options,
        AccessToken accessToken
    ) throws WikipediaException {
        // Apply cosmetic changes
        String textToSave = applyCosmeticChanges.applyCosmeticChanges(page);
        boolean applyCosmetics = !textToSave.equals(page.getContent());

        // Upload new content to Wikipedia
        wikipediaPageRepository.save(
            page.getId(),
            sectionId,
            textToSave,
            page.getQueryTimestamp(),
            buildEditSummary(options, applyCosmetics),
            accessToken
        );

        // Mark page as reviewed in the database
        this.markAsReviewed(page.getId(), options, true);
    }

    void savePageWithNoChanges(WikipediaPageId pageId, ReviewOptions options) {
        // Mark page as reviewed in the database
        this.markAsReviewed(pageId, options, false);
    }

    private String buildEditSummary(ReviewOptions options, boolean applyCosmetics) {
        StringBuilder summary = new StringBuilder(EDIT_SUMMARY);
        if (options.getOptionsType() != ReviewOptionsType.NO_TYPE && !options.isReviewAllTypes()) {
            summary.append(": «").append(options.getType().getSubtype()).append('»');
        }
        if (applyCosmetics) {
            summary.append(" + ").append(COSMETIC_CHANGES);
        }
        return summary.toString();
    }

    private void markAsReviewed(WikipediaPageId pageId, ReviewOptions options, boolean updateDate) {
        String reviewer = options.getUser();
        switch (options.getOptionsType()) {
            case NO_TYPE:
                // Review all types in any case
                markAsReviewedNoType(pageId, reviewer);
                break;
            case TYPE_SUBTYPE:
                if (options.isReviewAllTypes()) {
                    markAsReviewedNoType(pageId, reviewer);
                } else {
                    markAsReviewedTypeSubtype(pageId, options, reviewer);
                }
                break;
            case CUSTOM:
                if (options.isReviewAllTypes()) {
                    markAsReviewedNoType(pageId, reviewer);
                }
                markAsReviewedCustom(pageId, options, reviewer);
                break;
        }

        if (updateDate) {
            pageRepository.updatePageLastUpdate(pageId, LocalDate.now());
        }
    }

    private void markAsReviewedNoType(WikipediaPageId pageId, String reviewer) {
        replacementTypeRepository.updateReviewerByPageAndType(pageId, null, reviewer);
    }

    private void markAsReviewedTypeSubtype(WikipediaPageId pageId, ReviewOptions options, String reviewer) {
        replacementTypeRepository.updateReviewerByPageAndType(pageId, options.getType(), reviewer);
    }

    private void markAsReviewedCustom(WikipediaPageId pageId, ReviewOptions options, String reviewer) {
        // Custom replacements don't exist in the database to be reviewed
        String subtype = options.getType().getSubtype();
        boolean cs = options.getCs() != null && Boolean.TRUE.equals(options.getCs());
        customRepository.updateReviewerByPageAndType(pageId, subtype, cs, reviewer);
    }
}
