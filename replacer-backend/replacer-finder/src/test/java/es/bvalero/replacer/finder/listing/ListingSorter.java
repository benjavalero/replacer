package es.bvalero.replacer.finder.listing;

import static es.bvalero.replacer.common.util.ReplacerUtils.LOCALE_ES;
import static org.apache.commons.lang3.StringUtils.SPACE;

import es.bvalero.replacer.common.util.FileOfflineUtils;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.Collator;
import java.util.Objects;

public class ListingSorter {

    public static void main(String[] args) {
        final String fileName = "/offline/composed-misspellings-es.txt";
        final Collator collator = Collator.getInstance(LOCALE_ES);
        collator.setStrength(Collator.PRIMARY);
        try {
            Files
                .readAllLines(Paths.get(Objects.requireNonNull(FileOfflineUtils.class.getResource(fileName)).toURI()))
                .stream()
                .filter(line -> line.startsWith(SPACE))
                .sorted(collator::compare)
                .forEach(System.out::println);
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
