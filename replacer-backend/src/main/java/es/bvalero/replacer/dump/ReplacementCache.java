package es.bvalero.replacer.dump;

import es.bvalero.replacer.replacement.ReplacementEntity;
import es.bvalero.replacer.replacement.ReplacementIndexService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

@Slf4j
@Component
class ReplacementCache {
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Value("${replacer.dump.batch.chunk.size}")
    private int chunkSize;

    private int maxCachedId = 0;
    private ListValuedMap<Integer, ReplacementEntity> replacementMap = new ArrayListValuedHashMap<>(chunkSize);

    List<ReplacementEntity> findByArticleId(int articleId) {
        // Load the cache the first time or when needed
        if (maxCachedId == 0 || articleId > maxCachedId) {
            clean();

            int minId = maxCachedId + 1;
            while (articleId > maxCachedId) {
                // In case there is a gap greater than 1000 (CACHE SIZE) between DB Replacement IDs
                maxCachedId += chunkSize;
            }
            load(minId, maxCachedId);
        }

        // We create a new collection in order not to lose the items after removing the key from the map
        List<ReplacementEntity> replacements = new ArrayList<>(replacementMap.get(articleId));
        replacementMap.remove(articleId); // No need to check if the ID exists

        return replacements;
    }

    private void load(int minId, int maxId) {
        LOGGER.debug("START Load replacements from database to cache. Article ID between {} and {}", minId, maxId);
        findByArticles(minId, maxId)
            .forEach(replacement -> replacementMap.put(replacement.getArticleId(), replacement));
        LOGGER.debug("END Load replacements from database to cache. Articles cached: {}", replacementMap.size());
    }

    private void clean() {
        // Clear the cache if obsolete (we assume the dump articles are in order)
        // The remaining cached articles are not in the dump so we "remove" them from DB
        Set<Integer> obsoleteIds = new HashSet<>(replacementMap.keySet());
        // If all the replacements of an article are already reviewed by the system
        // there is no need to do it again
        Set<Integer> notReviewedIds = obsoleteIds
            .stream()
            .filter(id -> anyReplacementNotReviewed(replacementMap.get(id)))
            .collect(Collectors.toSet());
        LOGGER.debug("START Delete obsolete and not reviewed articles in DB: {}", notReviewedIds);
        reviewArticlesReplacementsAsSystem(notReviewedIds);
        replacementMap.clear();
        LOGGER.debug("END Delete obsolete and not reviewed articles in DB");
    }

    private boolean anyReplacementNotReviewed(List<ReplacementEntity> list) {
        return list.stream().anyMatch(item -> item.getReviewer() == null);
    }

    private List<ReplacementEntity> findByArticles(int minId, int maxId) {
        String sql = "SELECT * FROM replacement2 WHERE article_id BETWEEN :minId AND :maxId";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue("minId", minId)
            .addValue("maxId", maxId);
        return jdbcTemplate.query(sql, namedParameters, new ReplacementRowMapper());
    }

    private void reviewArticlesReplacementsAsSystem(Set<Integer> articleIds) {
        String sql =
            "UPDATE replacement2 SET reviewer=:system, last_update=:now " +
            "WHERE article_id IN (:articleIds) AND reviewer IS NULL";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue("system", ReplacementIndexService.SYSTEM_REVIEWER)
            .addValue("now", LocalDate.now())
            .addValue("articleIds", articleIds);
        jdbcTemplate.update(sql, namedParameters);
    }

    public void finish() {
        LOGGER.debug("Finish Replacement Cache. Reset maxCacheId and Clean.");
        this.clean();
        this.maxCachedId = 0;
    }
}
