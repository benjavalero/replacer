package es.bvalero.replacer.page.save;

import es.bvalero.replacer.checkwikipedia.CheckWikipediaService;
import es.bvalero.replacer.common.util.ReplacerUtils;
import es.bvalero.replacer.finder.Cosmetic;
import es.bvalero.replacer.finder.FinderPage;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
class ApplyCosmeticsService {

    // Dependency injection
    private final CosmeticFindService cosmeticFindService;
    private final CheckWikipediaService checkWikipediaService;

    ApplyCosmeticsService(CosmeticFindService cosmeticFindService, CheckWikipediaService checkWikipediaService) {
        this.cosmeticFindService = cosmeticFindService;
        this.checkWikipediaService = checkWikipediaService;
    }

    /** Return the new content of the page after applying all the cosmetic changes */
    String applyCosmeticChanges(FinderPage page) {
        String fixedText = page.getContent();
        Collection<Cosmetic> cosmeticFound = cosmeticFindService.findCosmetics(page);

        if (!cosmeticFound.isEmpty()) {
            // We can assume the collection is sorted
            // We apply the cosmetic replacements sorted in descending order by the start.
            // Therefore, we reverse the collection and just in case we sort it again.
            List<Cosmetic> cosmetics = new LinkedList<>(cosmeticFound);
            Collections.reverse(cosmetics);
            cosmetics.sort(Collections.reverseOrder());

            for (Cosmetic cosmetic : cosmetics) {
                try {
                    LOGGER.debug("START Apply cosmetic: {}", cosmetic);
                    fixedText = ReplacerUtils.replaceInText(
                        fixedText,
                        cosmetic.getStart(),
                        cosmetic.getText(),
                        cosmetic.getFix()
                    );
                    applyCheckWikipediaAction(page, cosmetic);
                    LOGGER.debug("END Apply cosmetic");
                } catch (Exception e) {
                    // Handle and continue
                    LOGGER.error("ERROR Apply cosmetic", e);
                }
            }

            // Apply cosmetics recursively in case a new one has appeared after applying others
            fixedText = applyCosmeticChanges(page.withContent(fixedText));
        }

        return fixedText;
    }

    private void applyCheckWikipediaAction(FinderPage page, Cosmetic cosmetic) {
        checkWikipediaService.reportFix(
            page.getPageKey().getLang(),
            page.getTitle(),
            cosmetic.getCheckWikipediaAction()
        );
    }
}
