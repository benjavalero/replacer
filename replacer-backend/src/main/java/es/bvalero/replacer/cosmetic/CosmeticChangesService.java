package es.bvalero.replacer.cosmetic;

import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CosmeticChangesService {
    @Autowired
    private List<CosmeticFinder> cosmeticFinders;

    public String applyCosmeticChanges(String text) {
        String fixedTest = text;
        for (CosmeticFinder finder : cosmeticFinders) {
            List<Cosmetic> cosmetics = finder.findList(fixedTest);

            // By default the results are sorted in descending order by the start
            Collections.sort(cosmetics);
            for (Cosmetic cosmetic : cosmetics) {
                fixedTest = replaceInText(cosmetic, fixedTest);
            }
        }
        return fixedTest;
    }

    private String replaceInText(Cosmetic cosmetic, String text) {
        int start = cosmetic.getStart();
        String oldText = cosmetic.getText();

        // Check just in case that the cosmetic is correct
        String current = text.substring(start, start + oldText.length());
        if (!current.equals(oldText)) {
            throw new IllegalArgumentException("Wrong cosmetic: " + current + " - " + oldText);
        }

        String newText = cosmetic.getFix();
        return text.substring(0, start) + newText + text.substring(start + oldText.length());
    }
}
