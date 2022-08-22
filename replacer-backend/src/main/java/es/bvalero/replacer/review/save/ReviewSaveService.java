package es.bvalero.replacer.review.save;

import es.bvalero.replacer.common.domain.*;
import es.bvalero.replacer.repository.*;
import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.WikipediaPageRepository;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
    private PageIndexRepository pageIndexRepository;

    @Autowired
    private CustomRepository customRepository;

    @Autowired
    private WikipediaPageRepository wikipediaPageRepository;

    @Autowired
    private ApplyCosmeticsService applyCosmeticChanges;

    void saveReviewContent(
        WikipediaPage page,
        @Nullable Integer sectionId,
        Collection<ReviewedReplacement> reviewedReplacements,
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
            buildEditSummary(reviewedReplacements, applyCosmetics),
            accessToken
        );
    }

    private String buildEditSummary(Collection<ReviewedReplacement> reviewedReplacements, boolean applyCosmetics) {
        Collection<String> fixed = reviewedReplacements
            .stream()
            .filter(ReviewedReplacement::isFixed)
            .map(r -> "«" + r.getType().getSubtype() + "»")
            .collect(Collectors.toUnmodifiableSet());
        if (fixed.isEmpty()) {
            throw new IllegalArgumentException("No fixed replacements when building edit summary");
        }

        // The summary is truncated to 500 codepoints when the page is published
        // https://en.wikipedia.org/wiki/Help:Edit_summary#The_500-character_limit
        StringBuilder summary = new StringBuilder(EDIT_SUMMARY).append(": ").append(StringUtils.join(fixed, ", "));

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

        // Mark the custom replacements as reviewed
        reviewedReplacements
            .stream()
            .filter(r -> r.getType().getKind() == ReplacementKind.CUSTOM)
            .forEach(this::markCustomAsReviewed);

        // Mark the usual replacements as reviewed
        List<ReplacementModel> usualToReview = reviewedReplacements
            .stream()
            .filter(r -> r.getType().getKind() != ReplacementKind.CUSTOM)
            .map(this::mapReviewedReplacement)
            .collect(Collectors.toUnmodifiableList());
        replacementTypeRepository.updateReviewer(usualToReview);
    }

    private void markCustomAsReviewed(ReviewedReplacement reviewed) {
        // Add the page to the database in case it doesn't exist yet
        if (pageIndexRepository.findPageById(reviewed.getPageId()).isEmpty()) {
            pageRepository.addPages(List.of(buildNewPage(reviewed)));
        }
        customRepository.addCustom(mapReviewedCustom(reviewed));
    }

    private PageModel buildNewPage(ReviewedReplacement reviewed) {
        return PageModel
            .builder()
            .lang(reviewed.getPageId().getLang().getCode())
            .pageId(reviewed.getPageId().getPageId())
            .title("")
            .lastUpdate(LocalDate.now())
            .replacements(Collections.emptyList())
            .build();
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
