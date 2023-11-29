package es.bvalero.replacer.page.save;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import es.bvalero.replacer.common.domain.CustomType;
import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.page.IndexedPage;
import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.page.PageRepository;
import es.bvalero.replacer.replacement.CustomReplacementService;
import es.bvalero.replacer.replacement.IndexedReplacement;
import es.bvalero.replacer.replacement.ReplacementSaveRepository;
import es.bvalero.replacer.user.User;
import es.bvalero.replacer.user.UserId;
import es.bvalero.replacer.wikipedia.WikipediaException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.TestOnly;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
class ReviewSaveService {

    private static final String EDIT_SUMMARY = "Reemplazos con [[Usuario:Benjavalero/Replacer|Replacer]]";
    private static final String COSMETIC_CHANGES = "mejoras cosméticas";

    // Dependency injection
    private final PageRepository pageRepository;
    private final ReplacementSaveRepository replacementSaveRepository;
    private final CustomReplacementService customReplacementService;
    private final WikipediaPageSaveRepository wikipediaPageSaveRepository;

    @Setter(onMethod_ = @TestOnly)
    @Value("${replacer.max-editions-per-minute}")
    private int maxEditionsPerMinute;

    private final Cache<UserId, CircularFifoQueue<LocalDateTime>> cachedUserEditions = Caffeine
        .newBuilder()
        .expireAfterWrite(1, TimeUnit.HOURS)
        .build();

    ReviewSaveService(
        PageRepository pageRepository,
        ReplacementSaveRepository replacementSaveRepository,
        CustomReplacementService customReplacementService,
        WikipediaPageSaveRepository wikipediaPageSaveRepository
    ) {
        this.pageRepository = pageRepository;
        this.replacementSaveRepository = replacementSaveRepository;
        this.customReplacementService = customReplacementService;
        this.wikipediaPageSaveRepository = wikipediaPageSaveRepository;
    }

    void saveReviewContent(WikipediaPageSaveCommand pageSave, User user) throws WikipediaException {
        validateEditionsPerMinute(user);
        wikipediaPageSaveRepository.save(pageSave, user.getAccessToken());
    }

    private void validateEditionsPerMinute(User user) throws WikipediaException {
        if (user.isBot()) {
            // Bots are allowed to perform more editions per minute
            return;
        }

        UserId userId = user.getId();
        CircularFifoQueue<LocalDateTime> userEditions = cachedUserEditions.get(
            userId,
            id -> new CircularFifoQueue<>(maxEditionsPerMinute)
        );
        assert userEditions != null;

        LocalDateTime now = LocalDateTime.now();
        if (userEditions.isAtFullCapacity()) {
            LocalDateTime older = userEditions.peek();
            assert older != null;
            LOGGER.debug("Older edition: {}", older);
            LOGGER.debug("Difference in seconds: {}", ChronoUnit.SECONDS.between(older, now));
            long diffMinutes = ChronoUnit.MINUTES.between(older, now);
            if (diffMinutes == 0) {
                LOGGER.error(
                    "Maximum number of editions per minute is {} - {} - {}",
                    maxEditionsPerMinute,
                    userId,
                    older
                );
                // The message is in Spanish to be displayed in an alert in the frontend
                throw new WikipediaException(
                    String.format("Ha sobrepasado el máximo de %d ediciones por minuto.", maxEditionsPerMinute)
                );
            }
        }

        userEditions.add(now);
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
        return switch (type.getKind()) {
            case SIMPLE, COMPOSED, CUSTOM -> "«" + type.getSubtype() + "»";
            case EMPTY -> throw new IllegalArgumentException();
            default -> type.getSubtype();
        };
    }

    void markAsReviewed(Collection<ReviewedReplacement> reviewedReplacements, boolean updateDate) {
        if (updateDate) {
            PageKey pageKey = reviewedReplacements
                .stream()
                .findAny()
                .orElseThrow(IllegalArgumentException::new)
                .getPageKey();
            pageRepository.updateLastUpdate(pageKey, LocalDate.now());
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
            .toList();
        replacementSaveRepository.updateReviewer(usualToReview);
    }

    private void markCustomAsReviewed(ReviewedReplacement reviewed) {
        // Add the page to the database in case it doesn't exist yet
        if (pageRepository.findByKey(reviewed.getPageKey()).isEmpty()) {
            IndexedPage indexedPage = IndexedPage
                .builder()
                .pageKey(reviewed.getPageKey())
                .title("") // It will be set in a next indexation
                .lastUpdate(LocalDate.now())
                .build();
            pageRepository.add(List.of(indexedPage));
        }
        customReplacementService.addCustomReplacement(reviewed.toCustomReplacement());
    }
}
