package es.bvalero.replacer.page;

import com.github.rozidan.springboot.logger.Loggable;
import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.ResultCount;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.logging.LogLevel;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Primary
@Transactional
@Repository
public class PageCountCacheRepository implements PageCountRepository {

    @Autowired
    @Qualifier("pageJdbcRepository")
    private PageCountRepository pageCountRepository;

    // Counts cache
    // It's a heavy query in database (several seconds), so we load the counts on start and refresh them periodically.
    // This may lead to a slight misalignment, which is fixed in the next refresh,
    // as modifications in the cache may happen while the count query is in progress.
    // We add synchronization just in case the list is requested while still loading on start.
    private Map<LangReplacementType, Integer> counts;

    @Override
    public Collection<ResultCount<ReplacementType>> countPagesNotReviewedByType(WikipediaLanguage lang) {
        return this.getCounts()
            .entrySet()
            .stream()
            .filter(entry -> entry.getKey().getLang().equals(lang))
            .map(entry -> ResultCount.of(entry.getKey().getType(), entry.getValue()))
            .collect(Collectors.toUnmodifiableList());
    }

    public void removePageCount(WikipediaLanguage lang, ReplacementType type) {
        this.counts.remove(LangReplacementType.of(lang, type));
    }

    public void incrementPageCount(WikipediaLanguage lang, ReplacementType type) {
        LangReplacementType langType = LangReplacementType.of(lang, type);
        this.counts.compute(langType, (lt, c) -> c == null ? 1 : c + 1);
    }

    public void decrementPageCount(WikipediaLanguage lang, ReplacementType type) {
        LangReplacementType langType = LangReplacementType.of(lang, type);
        this.counts.computeIfPresent(langType, (lt, c) -> c > 1 ? c - 1 : null);
    }

    /* SCHEDULED UPDATE OF CACHE */

    private synchronized Map<LangReplacementType, Integer> getCounts() {
        while (this.counts == null) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.error("Error getting the synchronized counts");
                return Collections.emptyMap();
            }
        }
        return this.counts;
    }

    @Loggable(value = LogLevel.DEBUG, skipArgs = true, skipResult = true)
    @Scheduled(fixedDelay = 3600000)
    public void scheduledUpdateReplacementCount() {
        loadReplacementTypeCounts();
    }

    private synchronized void loadReplacementTypeCounts() {
        this.counts =
            Arrays
                .stream(WikipediaLanguage.values())
                .map(this::toLangTypeCount)
                .flatMap(Collection::stream)
                .collect(
                    Collectors.toMap(ResultCount::getKey, ResultCount::getCount, (a, b) -> b, ConcurrentHashMap::new)
                );
        notifyAll();
    }

    private Collection<ResultCount<LangReplacementType>> toLangTypeCount(WikipediaLanguage lang) {
        return pageCountRepository
            .countPagesNotReviewedByType(lang)
            .stream()
            .map(rc -> ResultCount.of(LangReplacementType.of(lang, rc.getKey()), rc.getCount()))
            .collect(Collectors.toList());
    }

    @Value(staticConstructor = "of")
    private static class LangReplacementType {

        WikipediaLanguage lang;
        ReplacementType type;
    }
}
