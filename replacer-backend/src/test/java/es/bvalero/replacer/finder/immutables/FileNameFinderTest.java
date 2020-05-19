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
public class FileNameFinderTest {
    @Autowired
    private FileNameFinder fileNameFinder;

    @Test
    public void testFileName() {
        String filename1 = " xx.jpg ";
        String file1 = String.format("[[Archivo:%s|thumb|Description]]", filename1);
        String filename2 = "a b.png";
        String file2 = String.format("[[Imagen:%s]]", filename2);
        String text = String.format("%s %s", file1, file2);

        List<Immutable> matches = fileNameFinder.findList(text);

        Set<String> expected = new HashSet<>(Arrays.asList(filename1, filename2));
        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        Assertions.assertEquals(expected, actual);
    }
}
