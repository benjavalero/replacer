package es.bvalero.replacer.dump;

import com.jcabi.aspects.Loggable;
import es.bvalero.replacer.domain.ReplacerException;
import es.bvalero.replacer.domain.WikipediaLanguage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

/** Service to read a Wikipedia dump with a SAX parser, extract the pages and process them. */
@Slf4j
@Component
class DumpSaxParser implements DumpParser {

    @Autowired
    private DumpPageProcessor dumpPageProcessor;

    @Resource
    private Map<String, Long> numPagesEstimated;

    // Singleton properties to be set in each dump parsing
    // We assume we only parse one dump at a time
    private DumpSaxHandler dumpHandler;
    private Path dumpFile;

    @PostConstruct
    void setProperty() {
        System.setProperty("jdk.xml.totalEntitySizeLimit", Integer.toString(0));
    }

    @Override
    @Loggable(prepend = true, value = Loggable.DEBUG)
    public void parseDumpFile(WikipediaLanguage lang, Path dumpFile) throws ReplacerException {
        assert !getDumpIndexingStatus().isRunning();

        try (InputStream xmlInput = new BZip2CompressorInputStream(Files.newInputStream(dumpFile), true)) {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            saxParser.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            saxParser.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

            this.dumpFile = dumpFile;
            this.dumpHandler = new DumpSaxHandler(lang, dumpPageProcessor);
            saxParser.parse(xmlInput, dumpHandler);
        } catch (IOException e) {
            throw new ReplacerException("Dump file not valid", e);
        } catch (ParserConfigurationException | SAXException e) {
            throw new ReplacerException("SAX Error parsing dump file", e);
        }
    }

    @Override
    public DumpIndexingStatus getDumpIndexingStatus() {
        if (this.dumpHandler == null) {
            return DumpIndexingStatus.ofEmpty();
        } else {
            return DumpIndexingStatus
                .builder()
                .running(this.dumpHandler.isRunning())
                .numPagesRead(this.dumpHandler.getNumPagesRead())
                .numPagesProcessed(this.dumpHandler.getNumPagesProcessed())
                .numPagesEstimated(numPagesEstimated.get(this.dumpHandler.getLang().getCode()))
                .dumpFileName(this.dumpFile.getFileName().toString())
                .start(this.dumpHandler.getStart())
                .end(this.dumpHandler.getEnd())
                .build();
        }
    }
}
