package es.bvalero.replacer.finder.immutables;

import es.bvalero.replacer.XmlConfiguration;
import es.bvalero.replacer.finder.Immutable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = { FileNameFinder.class, XmlConfiguration.class })
class FileNameFinderTest {
    @Autowired
    private FileNameFinder fileNameFinder;

    @Test
    void testFileName() {
        String filename1 = " xx.jpg ";
        String file = String.format("[[Archivo:%s|thumb|Description]]", filename1);
        String filename2 = "a b.png";
        String image = String.format("[[Imagen:%s]]", filename2);
        String filename3 = "Z.JPEG";
        String fileLowercase = String.format("[[archivo:%s]]", filename3);
        String text = String.format("%s %s %s", file, image, fileLowercase);

        List<Immutable> matches = fileNameFinder.findList(text);

        Set<String> expected = new HashSet<>(Arrays.asList(filename2, filename3));
        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void testFileNameAliased() {
        String text = "[[Archivo:xxx.jpg|thumb|Description]]";

        List<Immutable> matches = fileNameFinder.findList(text);

        Assertions.assertTrue(matches.isEmpty());
    }
}
