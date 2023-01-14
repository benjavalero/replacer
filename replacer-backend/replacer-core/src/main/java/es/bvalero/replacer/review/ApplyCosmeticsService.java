package es.bvalero.replacer.review;

import es.bvalero.replacer.checkwikipedia.CheckWikipediaService;
import es.bvalero.replacer.finder.Cosmetic;
import es.bvalero.replacer.finder.CosmeticFindService;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
class ApplyCosmeticsService {

    @Autowired
    private CosmeticFindService cosmeticFindService;

    @Autowired
    private CheckWikipediaService checkWikipediaService;

    /** Return the new content of the page after applying all the cosmetic changes */
    String applyCosmeticChanges(WikipediaPage page) {
        String fixedText = page.getContent();
        Collection<Cosmetic> cosmeticFound = cosmeticFindService.findCosmetics(page);

        if (!cosmeticFound.isEmpty()) {
            // We can assume the collection is sorted
            // We apply the cosmetic replacements sorted in descending order by the start
            // Therefore we reverse the collection and just in case we sort it again
            List<Cosmetic> cosmetics = new LinkedList<>(cosmeticFound);
            Collections.reverse(cosmetics);
            cosmetics.sort(Collections.reverseOrder());

            for (Cosmetic cosmetic : cosmetics) {
                fixedText = replaceInText(cosmetic, fixedText);
                applyCheckWikipediaAction(page, cosmetic);
                LOGGER.debug("Cosmetic applied: {}", cosmetic);
            }
        }

        return fixedText;
    }

    private String replaceInText(Cosmetic cosmetic, String text) {
        String oldText = cosmetic.getText();
        String newText = cosmetic.getFix();
        assert text.substring(cosmetic.getStart(), cosmetic.getEnd()).equals(oldText);
        return text.substring(0, cosmetic.getStart()) + newText + text.substring(cosmetic.getEnd());
    }

    private void applyCheckWikipediaAction(WikipediaPage page, Cosmetic cosmetic) {
        checkWikipediaService.reportFix(
            page.getPageKey().getLang(),
            page.getTitle(),
            cosmetic.getCheckWikipediaAction()
        );
    }
}
