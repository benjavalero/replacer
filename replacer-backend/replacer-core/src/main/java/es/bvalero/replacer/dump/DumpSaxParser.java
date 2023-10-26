package es.bvalero.replacer.dump;

import es.bvalero.replacer.DumpProperties;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.page.index.PageIndexBatchService;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Optional;
import javax.annotation.PostConstruct;
import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

/** Service to read a Wikipedia dump with a SAX parser, extract the pages and index them. */
@Slf4j
@Component
class DumpSaxParser implements DumpParser {

    @Autowired
    private PageIndexBatchService pageIndexService;

    @Autowired
    private DumpProperties dumpProperties;

    // Singleton properties to be set in each dump parsing
    // We assume we only parse one dump at a time
    private DumpSaxHandler dumpHandler;
    private DumpFile dumpFile;

    @PostConstruct
    void setProperty() {
        System.setProperty("jdk.xml.totalEntitySizeLimit", Integer.toString(0));
    }

    @Override
    public void parseDumpFile(WikipediaLanguage lang, DumpFile dumpFile) throws ReplacerException {
        assert getDumpStatus().isEmpty() || !getDumpStatus().get().isRunning();

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

            this.dumpFile = dumpFile;
            this.dumpHandler = new DumpSaxHandler(lang, pageIndexService);
            saxParser.parse(xmlInput, this.dumpHandler);
        } catch (IOException e) {
            throw new ReplacerException("Dump file not valid: " + dumpFile, e);
        } catch (ParserConfigurationException | SAXException e) {
            throw new ReplacerException("SAX Error parsing dump file: " + dumpFile, e);
        }

        LOGGER.debug("END Parse dump file: {}", dumpFile);
    }

    @Override
    public Optional<DumpStatus> getDumpStatus() {
        if (this.dumpHandler == null || this.dumpHandler.getStart() == null) {
            return Optional.empty();
        } else {
            return Optional.of(
                DumpStatus
                    .builder()
                    .running(this.dumpHandler.isRunning())
                    .numPagesRead(this.dumpHandler.getNumPagesRead())
                    .numPagesIndexed(this.dumpHandler.getNumPagesIndexed())
                    .numPagesEstimated(
                        this.dumpProperties.getNumPagesEstimated().get(this.dumpHandler.getLang().getCode())
                    )
                    .dumpFileName(this.dumpFile.getPath().getFileName().toString())
                    .start(this.dumpHandler.getStart())
                    .end(this.dumpHandler.getEnd())
                    .build()
            );
        }
    }
}
