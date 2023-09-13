package es.bvalero.replacer.page.save;

import es.bvalero.replacer.common.domain.CustomType;
import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.page.IndexedPage;
import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.page.PageService;
import es.bvalero.replacer.replacement.CustomReplacementService;
import es.bvalero.replacer.replacement.IndexedReplacement;
import es.bvalero.replacer.replacement.ReplacementService;
import es.bvalero.replacer.user.AccessToken;
import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.WikipediaPageRepository;
import es.bvalero.replacer.wikipedia.WikipediaPageSave;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
class ReviewSaveService {

    private static final String EDIT_SUMMARY = "Reemplazos con [[Usuario:Benjavalero/Replacer|Replacer]]";
    private static final String COSMETIC_CHANGES = "mejoras cosméticas";

    @Autowired
    private PageService pageService;

    @Autowired
    private ReplacementService replacementService;

    @Autowired
    private CustomReplacementService customReplacementService;

    @Autowired
    private WikipediaPageRepository wikipediaPageRepository;

    void saveReviewContent(WikipediaPageSave pageSave, AccessToken accessToken) throws WikipediaException {
        wikipediaPageRepository.save(pageSave, accessToken);
    }

    String buildEditSummary(Collection<ReplacementType> fixedReplacementTypes, boolean applyCosmetics) {
        if (fixedReplacementTypes.isEmpty()) {
            throw new IllegalArgumentException("No fixed replacements when building edit summary");
        }

        // The summary is truncated to 500 codepoints when the page is published
        // https://en.wikipedia.org/wiki/Help:Edit_summary#The_500-character_limit
        Collection<String> fixed = fixedReplacementTypes
            .stream()
            .map(this::buildSubtypeSummary)
            .collect(Collectors.toUnmodifiableSet());
        StringBuilder summary = new StringBuilder(EDIT_SUMMARY).append(": ").append(StringUtils.join(fixed, ", "));

        if (applyCosmetics) {
            summary.append(" + ").append(COSMETIC_CHANGES);
        }
        return summary.toString();
    }

    private String buildSubtypeSummary(ReplacementType type) {
        switch (type.getKind()) {
            case SIMPLE:
            case COMPOSED:
            case CUSTOM:
                return "«" + type.getSubtype() + "»";
            case EMPTY:
                throw new IllegalArgumentException();
            default:
                return type.getSubtype();
        }
    }

    void markAsReviewed(Collection<ReviewedReplacement> reviewedReplacements, boolean updateDate) {
        if (updateDate) {
            PageKey pageKey = reviewedReplacements
                .stream()
                .findAny()
                .orElseThrow(IllegalArgumentException::new)
                .getPageKey();
            pageService.updatePageLastUpdate(pageKey, LocalDate.now());
        }

        // Mark the custom replacements as reviewed
        reviewedReplacements
            .stream()
            .filter(r -> r.getType() instanceof CustomType)
            .forEach(this::markCustomAsReviewed);

        // Mark the usual replacements as reviewed
        Collection<IndexedReplacement> usualToReview = reviewedReplacements
            .stream()
            .filter(r -> r.getType() instanceof StandardType)
            .map(ReviewedReplacement::toReplacement)
            .collect(Collectors.toUnmodifiableList());
        replacementService.updateReviewer(usualToReview);
    }

    private void markCustomAsReviewed(ReviewedReplacement reviewed) {
        // Add the page to the database in case it doesn't exist yet
        if (pageService.findPageByKey(reviewed.getPageKey()).isEmpty()) {
            IndexedPage indexedPage = IndexedPage
                .builder()
                .pageKey(reviewed.getPageKey())
                .title("") // It will be set in a next indexation
                .lastUpdate(LocalDate.now())
                .build();
            pageService.addPages(List.of(indexedPage));
        }
        customReplacementService.addCustomReplacement(reviewed.toCustomReplacement());
    }
}
