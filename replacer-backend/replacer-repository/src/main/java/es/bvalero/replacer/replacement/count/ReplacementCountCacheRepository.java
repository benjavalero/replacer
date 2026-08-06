package es.bvalero.replacer.replacement.count;

import static java.time.temporal.ChronoUnit.HOURS;
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
    private final Duration refreshTimeByPage = Duration.of(1, HOURS);

    // Dependency injection
    private ReplacementCountRepository replacementCountRepository;

    // Statistics caches
    // The queries in database can be heavy, so we preload the counts on start and refresh them periodically
    // via Caffeine (every 5 minutes). There is no real-time update with usage, as the periodic refresh
    // is frequent enough. If a request arrives before the initial load completes, Caffeine's LoadingCache
    // will block the request thread until the value is ready.
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
    // This query is very heavy so it is not worth updating it in real-time with usage.
    // Instead, it is preloaded with a delay after startup (to avoid competing with other heavy tasks)
    // and refreshed periodically via Caffeine every hour.
    private final LoadingCache<WikipediaLanguage, Collection<ResultCount<PageTitle>>> countGroupedByPage =
        Caffeine.newBuilder()
            .refreshAfterWrite(refreshTimeByPage)
            .build(l -> replacementCountRepository.countNotReviewedGroupedByPage(l, ReplacementCountApi.NUM_RESULTS));

    ReplacementCountCacheRepository(
        @Qualifier("replacementCountJdbcRepository") ReplacementCountRepository replacementCountRepository
    ) {
        this.replacementCountRepository = replacementCountRepository;
    }

    @Scheduled(initialDelay = 0, fixedDelay = Long.MAX_VALUE)
    public void init() {
        // Initial population of the caches
        Arrays.stream(WikipediaLanguage.values()).forEach(this::countReviewed);
        Arrays.stream(WikipediaLanguage.values()).forEach(this::countNotReviewed);
        Arrays.stream(WikipediaLanguage.values()).forEach(this::countReviewedGroupedByReviewer);
    }

    @Scheduled(initialDelayString = "${replacer.count.page.delay.initial}", fixedDelay = Long.MAX_VALUE)
    public void initCountGroupedByPage() {
        // Delayed population to avoid competing with other heavy startup tasks (e.g. page count cache)
        Arrays.stream(WikipediaLanguage.values()).forEach(l ->
            countNotReviewedGroupedByPage(l, ReplacementCountApi.NUM_RESULTS)
        );
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
        return Objects.requireNonNull(this.countGroupedByPage.get(lang));
    }
}
