package es.bvalero.replacer.finder.cosmetic;

import es.bvalero.replacer.finder.Finder;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.FinderService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CosmeticFinderService implements FinderService<Cosmetic> {

    @Autowired
    private List<CosmeticFinder> cosmeticFinders;

    @Override
    public Iterable<Finder<Cosmetic>> getFinders() {
        return new ArrayList<>(cosmeticFinders);
    }

    /**
     * @return the page text with all the cosmetic changes applied.
     */
    public String applyCosmeticChanges(FinderPage page) {
        String fixedText = page.getContent();
        List<Cosmetic> cosmetics = new LinkedList<>(this.find(page));

        if (!cosmetics.isEmpty()) {
            Collections.sort(cosmetics);

            // By default, the results are sorted in descending order by the start.
            for (Cosmetic cosmetic : cosmetics) {
                fixedText = replaceInText(cosmetic, fixedText);
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
