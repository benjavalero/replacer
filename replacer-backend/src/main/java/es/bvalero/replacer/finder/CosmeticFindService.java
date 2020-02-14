package es.bvalero.replacer.finder;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CosmeticFindService {
    @Autowired
    private List<CosmeticFinder> cosmeticFinders;

    public String applyCosmeticChanges(String text) {
        String fixedTest = text;
        for (CosmeticFinder finder : cosmeticFinders) {
            List<Cosmetic> cosmetics = finder.findStream(fixedTest).sorted().collect(Collectors.toList());

            // By default the results are sorted in descending order by the start
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
