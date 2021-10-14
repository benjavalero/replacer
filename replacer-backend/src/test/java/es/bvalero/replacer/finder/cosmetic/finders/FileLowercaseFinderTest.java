package es.bvalero.replacer.finder.cosmetic.finders;

import static org.junit.jupiter.api.Assertions.assertEquals;

import es.bvalero.replacer.config.XmlConfiguration;
import es.bvalero.replacer.finder.cosmetic.Cosmetic;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = { FileLowercaseFinder.class, XmlConfiguration.class })
class FileLowercaseFinderTest {

    @Autowired
    private FileLowercaseFinder fileLowercaseFinder;

    @Test
    void testFileLowercaseFinder() {
        String file1 = "archivo";
        String file2 = "image";
        String file3 = "File";
        String text = String.format("En [[%s:x.jpeg|test]] [[%s:x.png]] [[%s:x.pdf]].", file1, file2, file3);

        List<Cosmetic> matches = fileLowercaseFinder.findList(text);

        Set<String> expectedMatches = Set.of(file1, file2);
        Set<String> actualMatches = matches.stream().map(Cosmetic::getText).collect(Collectors.toSet());
        assertEquals(expectedMatches, actualMatches);

        String fix1 = "Archivo";
        String fix2 = "Image";
        Set<String> expectedFixes = Set.of(fix1, fix2);
        Set<String> actualFixes = matches.stream().map(Cosmetic::getFix).collect(Collectors.toSet());
        assertEquals(expectedFixes, actualFixes);
    }
}
