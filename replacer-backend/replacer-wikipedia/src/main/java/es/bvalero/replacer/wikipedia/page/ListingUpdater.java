package es.bvalero.replacer.wikipedia.page;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Executable class to download the listing pages from Wikipedia (Spanish and Galician)
 * and overwrite the local offline files used for testing and development.
 *
 * <p>Run from the IDE or from the replacer-backend directory.
 */
public class ListingUpdater {

    private static final Path OFFLINE_PATH = Paths.get("replacer-finder/src/main/resources/offline");

    private static final Map<String, Map<WikipediaLanguage, String>> LISTINGS = Map.of(
        "misspelling-list",
        Map.of(
            WikipediaLanguage.SPANISH,
            "Wikipedia:Corrector ortográfico/Listado",
            WikipediaLanguage.GALICIAN,
            "Wikipedia:Revisor ortográfico/Listaxe"
        ),
        "composed-misspellings",
        Map.of(
            WikipediaLanguage.SPANISH,
            "Usuario:Benjavalero/ComposedMisspellings",
            WikipediaLanguage.GALICIAN,
            "Usuario:Benjavalero/ComposedMisspellings"
        ),
        "false-positives",
        Map.of(
            WikipediaLanguage.SPANISH,
            "Usuario:Benjavalero/FalsePositives",
            WikipediaLanguage.GALICIAN,
            "Usuario:Benjavalero/FalsePositives"
        )
    );

    public static void main(String[] args) throws Exception {
        ConfigurableApplicationContext context = new SpringApplicationBuilder()
            .sources(getSourceClasses())
            .web(WebApplicationType.NONE)
            .run(args);

        WikipediaPageApiRepository repository = context.getBean(WikipediaPageApiRepository.class);

        System.out.println("Updating offline listings from Wikipedia...");
        System.out.println();

        for (Map.Entry<String, Map<WikipediaLanguage, String>> listing : LISTINGS.entrySet()) {
            String filePrefix = listing.getKey();
            for (Map.Entry<WikipediaLanguage, String> page : listing.getValue().entrySet()) {
                WikipediaLanguage lang = page.getKey();
                String pageTitle = page.getValue();

                System.out.printf("Downloading [%s] %s ...%n", lang.getCode(), pageTitle);

                WikipediaPage wikiPage = repository
                    .findByTitle(lang, pageTitle)
                    .orElseThrow(() -> new RuntimeException("Page not found: [" + lang.getCode() + "] " + pageTitle));

                String fileName = filePrefix + "-" + lang.getCode() + ".txt";
                Path filePath = OFFLINE_PATH.resolve(fileName);
                Files.writeString(filePath, wikiPage.getContent(), StandardCharsets.UTF_8);

                System.out.printf("  Written: %s (%d chars)%n", filePath, wikiPage.getContent().length());
            }
        }

        System.out.println();
        System.out.println("Done!");

        context.close();
    }

    private static Class<?>[] getSourceClasses() {
        try {
            return new Class<?>[] {
                WikipediaPageApiRepository.class,
                Class.forName("es.bvalero.replacer.wikipedia.api.WikipediaApiHelper"),
                Class.forName("es.bvalero.replacer.MediaWikiConfiguration"),
                Class.forName("es.bvalero.replacer.JsonMapperConfiguration"),
                Class.forName("es.bvalero.replacer.WikipediaPropertiesConfiguration"),
            };
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
