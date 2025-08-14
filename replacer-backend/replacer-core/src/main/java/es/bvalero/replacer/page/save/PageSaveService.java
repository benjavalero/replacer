package es.bvalero.replacer.page.save;

import es.bvalero.replacer.common.domain.User;
import es.bvalero.replacer.finder.CosmeticApi;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.ReplacementType;
import es.bvalero.replacer.page.IndexedPage;
import es.bvalero.replacer.page.PageSaveRepository;
import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.WikipediaPageSaveCommand;
import es.bvalero.replacer.wikipedia.WikipediaPageSaveRepository;
import es.bvalero.replacer.wikipedia.WikipediaPageSaveResult;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
class PageSaveService implements PageSaveApi {

    // Dependency injection
    private final CosmeticApi cosmeticApi;
    private final EditionsPerMinuteValidator editionsPerMinuteValidator;
    private final WikipediaPageSaveRepository wikipediaPageSaveRepository;
    private final PageSaveRepository pageSaveRepository;

    PageSaveService(
        CosmeticApi cosmeticApi,
        EditionsPerMinuteValidator editionsPerMinuteValidator,
        WikipediaPageSaveRepository wikipediaPageSaveRepository,
        PageSaveRepository pageSaveRepository
    ) {
        this.cosmeticApi = cosmeticApi;
        this.editionsPerMinuteValidator = editionsPerMinuteValidator;
        this.wikipediaPageSaveRepository = wikipediaPageSaveRepository;
        this.pageSaveRepository = pageSaveRepository;
    }

    @Override
    public void save(ReviewedPage reviewedPage, User user) throws WikipediaException {
        if (reviewedPage.isReviewedWithoutChanges()) {
            saveWithoutChanges(reviewedPage);
        } else {
            saveWithChanges(reviewedPage, user);
        }
    }

    private void saveWithoutChanges(ReviewedPage reviewedPage) {
        markAsReviewed(reviewedPage, null);
    }

    private void saveWithChanges(ReviewedPage reviewedPage, User user) throws WikipediaException {
        FinderPage page = reviewedPage.toFinderPage();

        // Apply cosmetic changes
        String textToSave = cosmeticApi.applyCosmeticChanges(page).getContent();
        boolean applyCosmetics = !textToSave.equals(reviewedPage.getContent());

        // Edit summary
        Collection<ReplacementType> fixedReplacementTypes = reviewedPage
            .getReviewedReplacements()
            .stream()
            .filter(ReviewedReplacement::isFixed)
            .map(ReviewedReplacement::getType)
            .collect(Collectors.toUnmodifiableSet());
        String summary = EditSummaryBuilder.build(fixedReplacementTypes, applyCosmetics);

        // Upload new content to Wikipedia
        WikipediaPageSaveCommand pageSave = WikipediaPageSaveCommand.builder()
            .pageKey(reviewedPage.getPageKey())
            .sectionId(reviewedPage.getSectionId())
            .content(textToSave)
            .editSummary(summary)
            .queryTimestamp(Objects.requireNonNull(reviewedPage.getQueryTimestamp()))
            .build();

        WikipediaPageSaveResult pageSaveResult = saveReviewContent(pageSave, user);
        markAsReviewed(reviewedPage, pageSaveResult);
    }

    private WikipediaPageSaveResult saveReviewContent(WikipediaPageSaveCommand pageSave, User user)
        throws WikipediaException {
        editionsPerMinuteValidator.validate(user);
        return wikipediaPageSaveRepository.save(pageSave, user.getAccessToken());
    }

    private void markAsReviewed(ReviewedPage reviewedPage, @Nullable WikipediaPageSaveResult saveResult) {
        IndexedPage indexedPage = reviewedPage.toIndexedPage(saveResult);
        pageSaveRepository.save(Collections.singletonList(indexedPage));
    }
}
