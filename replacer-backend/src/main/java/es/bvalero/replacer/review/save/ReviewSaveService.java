package es.bvalero.replacer.review.save;

import es.bvalero.replacer.common.domain.*;
import es.bvalero.replacer.repository.*;
import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.WikipediaPageRepository;
import java.time.LocalDate;
import java.util.Collection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
class ReviewSaveService {

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

    void saveReviewContent(
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
    }

    private String buildEditSummary(ReviewOptions options, boolean applyCosmetics) {
        StringBuilder summary = new StringBuilder(EDIT_SUMMARY);
        if (options.getOptionsType() != ReviewOptionsType.NO_TYPE) {
            summary.append(": «").append(options.getType().getSubtype()).append('»');
        }
        if (applyCosmetics) {
            summary.append(" + ").append(COSMETIC_CHANGES);
        }
        return summary.toString();
    }

    void markAsReviewed(Collection<ReviewedReplacement> reviewedReplacements, boolean updateDate) {
        if (updateDate) {
            WikipediaPageId pageId = reviewedReplacements
                .stream()
                .findAny()
                .orElseThrow(IllegalArgumentException::new)
                .getPageId();
            pageRepository.updatePageLastUpdate(pageId, LocalDate.now());
        }

        reviewedReplacements.forEach(this::markAsReviewed);
    }

    private void markAsReviewed(ReviewedReplacement reviewed) {
        switch (reviewed.getType().getKind()) {
            case EMPTY:
                throw new IllegalArgumentException("Unexpected empty replacement kind on saving review");
            case CUSTOM:
                customRepository.updateReviewer(mapReviewedCustom(reviewed));
                break;
            default:
                replacementTypeRepository.updateReviewer(mapReviewedReplacement(reviewed));
                break;
        }
    }

    private CustomModel mapReviewedCustom(ReviewedReplacement reviewed) {
        assert reviewed.getCs() != null;
        return CustomModel
            .builder()
            .lang(reviewed.getPageId().getLang().getCode())
            .pageId(reviewed.getPageId().getPageId())
            .replacement(reviewed.getType().getSubtype())
            .cs((byte) (Boolean.TRUE.equals(reviewed.getCs()) ? 1 : 0))
            .start(reviewed.getStart())
            .context("") // It is not important in this action
            .reviewer(reviewed.getReviewer())
            .build();
    }

    private ReplacementModel mapReviewedReplacement(ReviewedReplacement reviewed) {
        assert reviewed.getCs() == null;
        return ReplacementModel
            .builder()
            .lang(reviewed.getPageId().getLang().getCode())
            .pageId(reviewed.getPageId().getPageId())
            .kind(reviewed.getType().getKind().getCode())
            .subtype(reviewed.getType().getSubtype())
            .start(reviewed.getStart())
            .context("") // It is not important in this action
            .reviewer(reviewed.getReviewer())
            .build();
    }
}
