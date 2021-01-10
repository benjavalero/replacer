package es.bvalero.replacer.finder;

import es.bvalero.replacer.page.IndexablePage;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CosmeticFindService {

    private static final String CHECK_WIKIPEDIA_URL = "https://checkwiki.toolforge.org/cgi-bin/checkwiki.cgi";

    @Autowired
    private List<CosmeticFinder> cosmeticFinders;

    public String applyCosmeticChanges(IndexablePage page) {
        String fixedText = page.getContent();
        for (CosmeticFinder finder : cosmeticFinders) {
            List<Cosmetic> cosmetics = finder.findList(fixedText);
            if (!cosmetics.isEmpty()) {
                Collections.sort(cosmetics);

                // By default the results are sorted in descending order by the start
                for (Cosmetic cosmetic : cosmetics) {
                    fixedText = replaceInText(cosmetic, fixedText);
                }

                // Send confirmation to Check-Wikipedia
                finder.getFixId().ifPresent(fixId -> reportCheckWikipedia(page.getLang(), fixId, page.getTitle()));
            }
        }
        return fixedText;
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

    private void reportCheckWikipedia(WikipediaLanguage lang, int fixId, String title) {
        String project = String.format("%swiki", lang.getCode());

        RestTemplate restTemplate = new RestTemplate();
        String url = CHECK_WIKIPEDIA_URL + "?project=" + project + "&view=only&id=" + fixId + "&title=" + title;
        restTemplate.getForObject(url, String.class);
    }
}
