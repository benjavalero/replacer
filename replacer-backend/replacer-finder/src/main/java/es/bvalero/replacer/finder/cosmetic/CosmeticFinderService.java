package es.bvalero.replacer.finder.cosmetic;

import es.bvalero.replacer.checkwikipedia.CheckWikipediaService;
import es.bvalero.replacer.common.util.ReplacerUtils;
import es.bvalero.replacer.finder.*;
import jakarta.annotation.PostConstruct;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
class CosmeticFinderService implements FinderService<Cosmetic>, CosmeticApi {

    // Dependency injection
    private final List<CosmeticFinder> cosmeticFinders;
    private final CheckWikipediaService checkWikipediaService;

    public CosmeticFinderService(List<CosmeticFinder> cosmeticFinders, CheckWikipediaService checkWikipediaService) {
        this.cosmeticFinders = cosmeticFinders;
        this.checkWikipediaService = checkWikipediaService;
    }

    @PostConstruct
    public void sortImmutableFinders() {
        Collections.sort(cosmeticFinders);
    }

    @Override
    public Iterable<Finder<Cosmetic>> getFinders() {
        return new ArrayList<>(cosmeticFinders);
    }

    @Override
    public Collection<Cosmetic> findCosmetics(FinderPage page) {
        return this.find(page);
    }

    /** Return the page with new content after applying all the cosmetic changes */
    @Override
    public FinderPage applyCosmeticChanges(FinderPage page) {
        FinderPage fixedPage = page;
        Collection<Cosmetic> cosmeticFound = findCosmetics(page);

        if (!cosmeticFound.isEmpty()) {
            // We can assume the collection is sorted
            // We apply the cosmetic replacements sorted in descending order by the start.
            // Therefore, we reverse the collection and just in case we sort it again.
            List<Cosmetic> cosmetics = new LinkedList<>(cosmeticFound);
            Collections.reverse(cosmetics);
            cosmetics.sort(Collections.reverseOrder());

            for (Cosmetic cosmetic : cosmetics) {
                fixedPage = applyCosmetic(fixedPage, cosmetic);
            }

            // Apply cosmetics recursively in case a new one has appeared after applying others
            fixedPage = applyCosmeticChanges(fixedPage);
        }

        return fixedPage;
    }

    private FinderPage applyCosmetic(FinderPage page, Cosmetic cosmetic) {
        try {
            LOGGER.debug("START Apply cosmetic: {}", cosmetic);
            String fixedText = ReplacerUtils.replaceInText(
                page.getContent(),
                cosmetic.getStart(),
                cosmetic.getText(),
                cosmetic.getFix()
            );
            FinderPage fixedPage = page.withContent(fixedText);
            applyCheckWikipediaAction(page, cosmetic);
            LOGGER.debug("END Apply cosmetic");
            return fixedPage;
        } catch (Exception e) {
            // Handle and continue
            LOGGER.error("ERROR Apply cosmetic", e);
            return page;
        }
    }

    private void applyCheckWikipediaAction(FinderPage page, Cosmetic cosmetic) {
        checkWikipediaService.reportFix(
            page.getPageKey().getLang(),
            page.getTitle(),
            cosmetic.getCheckWikipediaAction()
        );
    }
}
