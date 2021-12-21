package es.bvalero.replacer.page.save;

import es.bvalero.replacer.common.domain.AccessToken;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.common.domain.WikipediaPageId;
import es.bvalero.replacer.page.review.PageReviewOptions;
import es.bvalero.replacer.page.review.PageReviewOptionsType;
import es.bvalero.replacer.repository.CustomModel;
import es.bvalero.replacer.repository.CustomRepository;
import es.bvalero.replacer.repository.PageRepository;
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
    private PageRepository pageRepository;

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
    ) throws WikipediaException {
        // Apply cosmetic changes
        String textToSave = applyCosmeticChanges.applyCosmeticChanges(page);
        boolean applyCosmetics = !textToSave.equals(page.getContent());

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
        this.markAsReviewed(page.getId(), options, true);
    }

    void savePageWithNoChanges(WikipediaPageId pageId, PageReviewOptions options) {
        // Mark page as reviewed in the database
        this.markAsReviewed(pageId, options, false);
    }

    private String buildEditSummary(PageReviewOptions options, boolean applyCosmetics) {
        StringBuilder summary = new StringBuilder(EDIT_SUMMARY);
        if (options.getOptionsType() != PageReviewOptionsType.NO_TYPE) {
            summary.append(": «").append(options.getType().getSubtype()).append('»');
        }
        if (applyCosmetics) {
            summary.append(" + ").append(COSMETIC_CHANGES);
        }
        return summary.toString();
    }

    private void markAsReviewed(WikipediaPageId pageId, PageReviewOptions options, boolean updateDate) {
        String reviewer = options.getUser();
        switch (options.getOptionsType()) {
            case NO_TYPE:
                markAsReviewedNoType(pageId, reviewer);
                break;
            case TYPE_SUBTYPE:
                markAsReviewedTypeSubtype(pageId, options, reviewer);
                break;
            case CUSTOM:
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

    private void markAsReviewedTypeSubtype(WikipediaPageId pageId, PageReviewOptions options, String reviewer) {
        replacementTypeRepository.updateReviewerByPageAndType(pageId, options.getType(), reviewer);
    }

    private void markAsReviewedCustom(WikipediaPageId pageId, PageReviewOptions options, String reviewer) {
        // Custom replacements don't exist in the database to be reviewed
        String subtype = options.getType().getSubtype();
        boolean cs = options.getCs() != null && Boolean.TRUE.equals(options.getCs());
        customRepository.addCustom(buildCustomReviewed(pageId, subtype, cs, reviewer));
    }

    private CustomModel buildCustomReviewed(
        WikipediaPageId wikipediaPageId,
        String replacement,
        boolean cs,
        String reviewer
    ) {
        return CustomModel
            .builder()
            .lang(wikipediaPageId.getLang().getCode())
            .pageId(wikipediaPageId.getPageId())
            .replacement(replacement)
            .cs((byte) (cs ? 1 : 0))
            .lastUpdate(LocalDate.now())
            .reviewer(reviewer)
            .build();
    }
}
