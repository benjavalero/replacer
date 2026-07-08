package es.bvalero.replacer.dump;

import es.bvalero.replacer.DumpProperties;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.index.PageIndexApi;
import jakarta.annotation.PostConstruct;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

/** Service to read Wikipedia dump files with a SAX parser, extract the pages and index them. */
@Slf4j
@Component
class DumpParser {

    // Dependency injection
    private final PageIndexApi pageIndexApi;
    private final DumpProperties dumpProperties;

    // Initialized before parsing starts, then only read during parsing.
    // No synchronization needed.
    private List<DumpSaxHandler> dumpHandlers = List.of();
    private List<DumpFile> dumpFiles = List.of();

    DumpParser(@Qualifier("pageIndexBatchService") PageIndexApi pageIndexApi, DumpProperties dumpProperties) {
        this.pageIndexApi = pageIndexApi;
        this.dumpProperties = dumpProperties;
    }

    @PostConstruct
    void setProperty() {
        System.setProperty("jdk.xml.totalEntitySizeLimit", Integer.toString(0));
    }

    void parseDumpFiles(WikipediaLanguage lang, Collection<DumpFile> dumpFiles) {
        if (dumpFiles.isEmpty()) {
            return;
        }
        assert getDumpStatus().isEmpty() || !getDumpStatus().get().isRunning();

        // Initialize handlers and files upfront so they are only read during parsing
        this.dumpFiles = new ArrayList<>(dumpFiles);
        this.dumpHandlers = dumpFiles
            .stream()
            .map(f -> new DumpSaxHandler(lang, pageIndexApi))
            .collect(Collectors.toCollection(ArrayList::new));

        // Parse dump files in parallel using the common ForkJoinPool.
        // No other task in the application uses it, so there is no contention.
        List<CompletableFuture<Void>> futures = IntStream.range(0, this.dumpFiles.size())
            .mapToObj(i ->
                CompletableFuture.runAsync(() -> parseDumpFile(this.dumpFiles.get(i), this.dumpHandlers.get(i)))
            )
            .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    private void parseDumpFile(DumpFile dumpFile, DumpSaxHandler dumpHandler) {
        LOGGER.debug("START Parse dump file: {} ...", dumpFile);
        // The use of the buffered input stream is on purpose
        // as it parses the dump about 5x faster
        try (
            InputStream xmlInput = new BZip2CompressorInputStream(
                new BufferedInputStream(Files.newInputStream(dumpFile.getPath())),
                true
            )
        ) {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            saxParser.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            saxParser.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

            saxParser.parse(xmlInput, dumpHandler);
        } catch (IOException e) {
            LOGGER.error("Dump file not valid: {}", dumpFile, e);
        } catch (ParserConfigurationException | SAXException e) {
            LOGGER.error("SAX Error parsing dump file: {}", dumpFile, e);
        }

        LOGGER.debug("END Parse dump file: {}", dumpFile);
    }

    Optional<DumpStatus> getDumpStatus() {
        if (this.dumpHandlers.isEmpty() || this.dumpHandlers.getFirst().getStart() == null) {
            return Optional.empty();
        }

        boolean anyRunning = this.dumpHandlers.stream().anyMatch(DumpSaxHandler::isRunning);
        int totalPagesRead = this.dumpHandlers.stream().mapToInt(DumpSaxHandler::getNumPagesRead).sum();
        int totalPagesIndexed = this.dumpHandlers.stream().mapToInt(DumpSaxHandler::getNumPagesIndexed).sum();

        String dumpFileName =
            this.dumpFiles.stream().map(f -> f.getPath().getFileName().toString()).collect(Collectors.joining(", "));

        LocalDateTime start =
            this.dumpHandlers.stream()
                .map(DumpSaxHandler::getStart)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        // End is null if any handler has not finished yet
        LocalDateTime end = anyRunning
            ? null
            : this.dumpHandlers.stream()
                .map(DumpSaxHandler::getEnd)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        return Optional.of(
            DumpStatus.builder()
                .running(anyRunning)
                .numPagesRead(totalPagesRead)
                .numPagesIndexed(totalPagesIndexed)
                .numPagesEstimated(
                    this.dumpProperties.getNumPagesEstimated().get(this.dumpHandlers.getFirst().getLang().getCode())
                )
                .dumpFileName(dumpFileName)
                .start(start)
                .end(end)
                .build()
        );
    }
}
