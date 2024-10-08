package es.bvalero.replacer.page.count;

import static java.time.temporal.ChronoUnit.HOURS;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import es.bvalero.replacer.common.domain.ResultCount;
import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Primary
@Transactional
@Repository
public class PageCountCacheRepository implements PageCountRepository {

    private final Duration refreshTime = Duration.of(1, HOURS);

    // Dependency injection
    private final PageCountRepository pageCountRepository;

    // Counts cache
    // It's a heavy query in database (several seconds), so we load the counts the first time and refresh them periodically.
    // This may lead to a slight misalignment, which is fixed in the next refresh,
    // as modifications in the cache may happen while the count query is in progress.
    // We could lock the cache while querying but the query takes too long.
    // In theory this cache is mirroring the database reality so in the future the duration could be even longer.
    private final LoadingCache<WikipediaLanguage, Map<StandardType, Integer>> counts = Caffeine.newBuilder()
        .refreshAfterWrite(refreshTime)
        .build(this::loadReplacementTypeCounts);

    public PageCountCacheRepository(@Qualifier("pageCountJdbcRepository") PageCountRepository pageCountRepository) {
        this.pageCountRepository = pageCountRepository;
    }

    @Scheduled(initialDelay = 0, fixedDelay = Long.MAX_VALUE)
    public void init() {
        // Initial population of the caches
        Iterable<WikipediaLanguage> keys = Arrays.asList(WikipediaLanguage.values());
        this.counts.getAll(keys);
    }

    @Override
    public Collection<ResultCount<StandardType>> countNotReviewedGroupedByType(WikipediaLanguage lang) {
        return this.getCounts(lang)
            .entrySet()
            .stream()
            .map(entry -> ResultCount.of(entry.getKey(), entry.getValue()))
            .toList();
    }

    private Map<StandardType, Integer> getCounts(WikipediaLanguage lang) {
        return Objects.requireNonNull(this.counts.get(lang));
    }

    @Override
    public int countNotReviewedByType(WikipediaLanguage lang, @Nullable StandardType type) {
        if (type == null) {
            return this.pageCountRepository.countNotReviewedByType(lang, null);
        } else {
            // Always return the cached count
            return Objects.requireNonNullElse(this.getCounts(lang).get(type), 0);
        }
    }

    public void removePageCountByType(WikipediaLanguage lang, StandardType type) {
        this.getCounts(lang).remove(type);
    }

    public void incrementPageCountByType(WikipediaLanguage lang, StandardType type) {
        this.getCounts(lang).compute(type, (t, c) -> c == null ? 1 : c + 1);
    }

    public void decrementPageCountByType(WikipediaLanguage lang, StandardType type) {
        this.getCounts(lang).computeIfPresent(type, (t, c) -> c > 1 ? c - 1 : null);
    }

    private Map<StandardType, Integer> loadReplacementTypeCounts(WikipediaLanguage lang) {
        LOGGER.debug("START Loading Replacement Type counts - {}…", lang);
        Map<StandardType, Integer> counts = pageCountRepository
            .countNotReviewedGroupedByType(lang)
            .stream()
            .collect(Collectors.toConcurrentMap(ResultCount::getKey, ResultCount::getCount));
        LOGGER.debug("END Loading Replacement Type counts - {}", lang);
        return counts;
    }
}
