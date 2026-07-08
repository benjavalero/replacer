package es.bvalero.replacer.dump;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DumpFinderTest {

    @TempDir
    Path dumpBaseFolder;

    private DumpFinder dumpFinder;

    @BeforeEach
    public void setUp() {
        dumpFinder = new DumpFinder();
    }

    private Path createDumpStructure(String date) throws Exception {
        Path project = dumpBaseFolder.resolve("eswiki");
        Files.createDirectories(project);
        Path dateFolder = project.resolve(date);
        Files.createDirectories(dateFolder);
        Path bzip2Folder = dateFolder.resolve("xml/bzip2");
        Files.createDirectories(bzip2Folder);
        return bzip2Folder;
    }

    @Test
    void testFindLatestDumpFiles() throws Exception {
        // Two date folders, each with one valid dump file
        Path bzip2Older = createDumpStructure("2026-06-01");
        Path bzip2Newer = createDumpStructure("2026-07-01");
        Files.createFile(bzip2Older.resolve("eswiki-2026-06-01-p1p100.xml.bz2"));
        Files.createFile(bzip2Newer.resolve("eswiki-2026-07-01-p1p100.xml.bz2"));

        dumpFinder.setDumpPathBase(dumpBaseFolder.toString());

        Collection<DumpFile> result = dumpFinder.findLatestDumpFiles(WikipediaLanguage.SPANISH);

        assertEquals(1, result.size());
        assertTrue(result.iterator().next().getPath().toString().contains("2026-07-01"));
    }

    @Test
    void testFindLatestDumpFilesInOldFolder() throws Exception {
        // Latest folder has no dump files yet
        Path bzip2Older = createDumpStructure("2026-06-01");
        createDumpStructure("2026-07-01"); // empty
        Files.createFile(bzip2Older.resolve("eswiki-2026-06-01-p1p100.xml.bz2"));

        dumpFinder.setDumpPathBase(dumpBaseFolder.toString());

        Collection<DumpFile> result = dumpFinder.findLatestDumpFiles(WikipediaLanguage.SPANISH);

        assertEquals(1, result.size());
        assertTrue(result.iterator().next().getPath().toString().contains("2026-06-01"));
    }

    @Test
    void testFindMultipleDumpFiles() throws Exception {
        Path bzip2 = createDumpStructure("2026-07-01");
        Files.createFile(bzip2.resolve("eswiki-2026-07-01-p5p5140731.xml.bz2"));
        Files.createFile(bzip2.resolve("eswiki-2026-07-01-p5140732p9913156.xml.bz2"));
        Files.createFile(bzip2.resolve("eswiki-2026-07-01-p9913157p11541945.xml.bz2"));

        dumpFinder.setDumpPathBase(dumpBaseFolder.toString());

        Collection<DumpFile> result = dumpFinder.findLatestDumpFiles(WikipediaLanguage.SPANISH);

        assertEquals(3, result.size());
    }

    @Test
    void testIgnoreNonMatchingFiles() throws Exception {
        Path bzip2 = createDumpStructure("2026-07-01");
        Files.createFile(bzip2.resolve("eswiki-2026-07-01-p5p5140731.xml.bz2"));
        Files.createFile(bzip2.resolve("some-other-file.txt")); // Non-valid

        dumpFinder.setDumpPathBase(dumpBaseFolder.toString());

        Collection<DumpFile> result = dumpFinder.findLatestDumpFiles(WikipediaLanguage.SPANISH);

        assertEquals(1, result.size());
    }

    @Test
    void testEmptyDumpFolder() throws Exception {
        createDumpStructure("2026-07-01"); // empty bzip2 folder

        dumpFinder.setDumpPathBase(dumpBaseFolder.toString());

        assertTrue(dumpFinder.findLatestDumpFiles(WikipediaLanguage.SPANISH).isEmpty());
    }

    @Test
    void testInvalidDateFolderIgnored() throws Exception {
        // Folder with invalid date format
        Path project = dumpBaseFolder.resolve("eswiki");
        Files.createDirectories(project);
        Path invalidFolder = project.resolve("20260701"); // old format, should be ignored
        Files.createDirectories(invalidFolder.resolve("xml/bzip2"));
        Files.createFile(invalidFolder.resolve("xml/bzip2/eswiki-20260701-p1p100.xml.bz2"));

        dumpFinder.setDumpPathBase(dumpBaseFolder.toString());

        assertTrue(dumpFinder.findLatestDumpFiles(WikipediaLanguage.SPANISH).isEmpty());
    }

    @Test
    void testNotExistingProjectFolder() {
        dumpFinder.setDumpPathBase(dumpBaseFolder.toString());

        assertTrue(dumpFinder.findLatestDumpFiles(WikipediaLanguage.SPANISH).isEmpty());
    }

    @Test
    void testNotExistingBaseFolder() {
        dumpFinder.setDumpPathBase("xxx");

        assertTrue(dumpFinder.findLatestDumpFiles(WikipediaLanguage.SPANISH).isEmpty());
    }
}
