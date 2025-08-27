package es.bvalero.replacer.replacement.count;

import static java.time.temporal.ChronoUnit.MINUTES;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import es.bvalero.replacer.common.domain.PageTitle;
import es.bvalero.replacer.common.domain.ResultCount;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Primary
@Transactional
@Repository
class ReplacementCountCacheRepository implements ReplacementCountRepository {

    private final Duration refreshTime = Duration.of(5, MINUTES);

    // Dependency injection
    private ReplacementCountRepository replacementCountRepository;

    // Statistics caches
    // The queries in database can be heavy, so we preload the counts on start and refresh them periodically.
    // As we refresh the stats every 5 minutes, it's not worth to try to update the counts at real-time.
    private final LoadingCache<WikipediaLanguage, Integer> countReviewed = Caffeine.newBuilder()
        .refreshAfterWrite(refreshTime)
        .build(l -> replacementCountRepository.countReviewed(l));
    private final LoadingCache<WikipediaLanguage, Integer> countNotReviewed = Caffeine.newBuilder()
        .refreshAfterWrite(refreshTime)
        .build(l -> replacementCountRepository.countNotReviewed(l));
    private final LoadingCache<WikipediaLanguage, Collection<ResultCount<String>>> countGroupedByReviewer =
        Caffeine.newBuilder()
            .refreshAfterWrite(refreshTime)
            .build(l -> replacementCountRepository.countReviewedGroupedByReviewer(l));

    ReplacementCountCacheRepository(
        @Qualifier("replacementCountJdbcRepository") ReplacementCountRepository replacementCountRepository
    ) {
        this.replacementCountRepository = replacementCountRepository;
    }

    @Scheduled(initialDelay = 0, fixedDelay = Long.MAX_VALUE)
    public void init() {
        // Initial population of the caches
        Iterable<WikipediaLanguage> keys = Arrays.asList(WikipediaLanguage.values());
        this.countReviewed.getAll(keys);
        this.countNotReviewed.getAll(keys);
        this.countGroupedByReviewer.getAll(keys);
    }

    @Override
    public int countReviewed(WikipediaLanguage lang) {
        return Objects.requireNonNull(this.countReviewed.get(lang));
    }

    @Override
    public int countNotReviewed(WikipediaLanguage lang) {
        return Objects.requireNonNull(this.countNotReviewed.get(lang));
    }

    @Override
    public Collection<ResultCount<String>> countReviewedGroupedByReviewer(WikipediaLanguage lang) {
        return Objects.requireNonNull(this.countGroupedByReviewer.get(lang));
    }

    @Override
    public Collection<ResultCount<PageTitle>> countNotReviewedGroupedByPage(WikipediaLanguage lang, int numResults) {
        // For the moment we are not going to cache it as it is used only by admins
        return replacementCountRepository.countNotReviewedGroupedByPage(lang, numResults);
    }
}
