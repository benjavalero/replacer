package es.bvalero.replacer.finder.listing;

import static es.bvalero.replacer.common.util.ReplacerUtils.LOCALE_ES;
import static es.bvalero.replacer.finder.util.FinderUtils.SPACE;

import es.bvalero.replacer.common.util.FileOfflineUtils;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.Collator;
import java.util.Objects;
import lombok.SneakyThrows;

public class ListingSorter {

    @SneakyThrows
    public static void main(String[] args) {
        final String fileName = "/offline/composed-misspellings-es.txt";
        final Collator collator = Collator.getInstance(LOCALE_ES);
        collator.setStrength(Collator.PRIMARY);
        Files.readAllLines(Paths.get(Objects.requireNonNull(FileOfflineUtils.class.getResource(fileName)).toURI()))
            .stream()
            .filter(line -> line.startsWith(SPACE))
            .sorted(collator::compare)
            .forEach(System.out::println);
    }
}
