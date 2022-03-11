package es.bvalero.replacer.page.save;

import es.bvalero.replacer.common.domain.Cosmetic;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.finder.cosmetic.CosmeticFinderService;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
class ApplyCosmeticsService {

    @Autowired
    private CosmeticFinderService cosmeticFinderService;

    @Autowired
    private CheckWikipediaService checkWikipediaService;

    /** Return the new content of the page after applying all the cosmetic changes */
    String applyCosmeticChanges(WikipediaPage page) {
        String fixedText = page.getContent();
        Set<Cosmetic> cosmeticFound = cosmeticFinderService.find(page);

        if (!cosmeticFound.isEmpty()) {
            // We can assume the found set is sorted and of course with no duplicates
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
        checkWikipediaService.reportFix(page.getId().getLang(), page.getTitle(), cosmetic.getCheckWikipediaAction());
    }
}
