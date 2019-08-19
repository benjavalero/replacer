package es.bvalero.replacer.dump;

import es.bvalero.replacer.article.ArticleIndexService;
import es.bvalero.replacer.article.ArticleService;
import es.bvalero.replacer.article.Replacement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
class DumpArticleCache {

    private static final int CACHE_SIZE = 1000;
    private int maxCachedId;
    private Map<Integer, Collection<Replacement>> replacementMap = new HashMap<>(CACHE_SIZE);

    @Autowired
    private ArticleService articleService;

    @Autowired
    private ArticleIndexService articleIndexService;

    Collection<Replacement> findDatabaseReplacements(int articleId) {
        // Load the cache the first time or when needed
        if (maxCachedId == 0 || articleId > maxCachedId) {
            clean();

            int minId = maxCachedId + 1;
            while (articleId > maxCachedId) {
                // In case there is a gap greater than 1000 (CACHE SIZE) between DB Replacement IDs
                maxCachedId += CACHE_SIZE;
            }
            load(minId, maxCachedId);
        }

        Collection<Replacement> replacements = replacementMap.getOrDefault(articleId, Collections.emptySet());
        replacementMap.remove(articleId); // No need to check if the ID exists

        return replacements;
    }

    private void load(int minId, int maxId) {
        LOGGER.debug("START Load replacements from database to cache. Article ID between {} and {}", minId, maxId);
        for (Replacement replacement : articleService.findDatabaseReplacementByArticles(minId, maxId)) {
            if (!replacementMap.containsKey(replacement.getArticleId())) {
                replacementMap.put(replacement.getArticleId(), new HashSet<>());
            }
            replacementMap.get(replacement.getArticleId()).add(replacement);
        }
        LOGGER.debug("END Load replacements from database to cache. Articles cached: {}", replacementMap.size());
    }

    void clean() {
        // Clear the cache if obsolete (we assume the dump articles are in order)
        // The remaining cached articles are not in the dump so we remove them from DB
        LOGGER.debug("START Delete obsolete articles in DB: {}", replacementMap.keySet());
        articleIndexService.reviewArticlesAsSystem(replacementMap.keySet());
        replacementMap = new HashMap<>(CACHE_SIZE);
        LOGGER.debug("END Delete obsolete articles in DB");
    }

}
