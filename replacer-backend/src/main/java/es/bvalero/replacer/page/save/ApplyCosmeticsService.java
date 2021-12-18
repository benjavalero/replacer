package es.bvalero.replacer.page.save;

import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.finder.FinderPageMapper;
import es.bvalero.replacer.finder.cosmetic.Cosmetic;
import es.bvalero.replacer.finder.cosmetic.CosmeticFinderService;
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
    private CosmeticFinderService cosmeticFinderService;

    /** Return the new content of the page after applying all the cosmetic changes */
    String applyCosmeticChanges(WikipediaPage page) {
        String fixedText = page.getContent();
        List<Cosmetic> cosmetics = new LinkedList<>(cosmeticFinderService.find(FinderPageMapper.fromDomain(page)));

        if (!cosmetics.isEmpty()) {
            Collections.sort(cosmetics);

            // By default, the results are sorted in descending order by the start.
            for (Cosmetic cosmetic : cosmetics) {
                fixedText = replaceInText(cosmetic, fixedText);
                LOGGER.debug("Cosmetic applied: {}", cosmetic);
            }
        }

        return fixedText;
    }

    private String replaceInText(Cosmetic cosmetic, String text) {
        int start = cosmetic.getStart();
        String oldText = cosmetic.getText();
        String newText = cosmetic.getFix();
        return text.substring(0, start) + newText + text.substring(start + oldText.length());
    }
}
