package es.bvalero.replacer.dump;

import com.github.rozidan.springboot.logger.Loggable;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.page.index.PageIndexer;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.logging.LogLevel;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

/** Service to read a Wikipedia dump with a SAX parser, extract the pages and index them. */
@Slf4j
@Component
class DumpSaxParser implements DumpParser {

    @Autowired
    @Qualifier("pageBatchIndexService")
    private PageIndexer pageIndexer;

    @Resource
    private Map<String, Long> numPagesEstimated;

    // Singleton properties to be set in each dump parsing
    // We assume we only parse one dump at a time
    private DumpSaxHandler dumpHandler;
    private DumpFile dumpFile;

    @PostConstruct
    void setProperty() {
        System.setProperty("jdk.xml.totalEntitySizeLimit", Integer.toString(0));
    }

    @Override
    @Loggable(value = LogLevel.DEBUG, entered = true, skipResult = true)
    public void parseDumpFile(WikipediaLanguage lang, DumpFile dumpFile) throws ReplacerException {
        assert !getDumpIndexingStatus().getRunning();

        try (InputStream xmlInput = new BZip2CompressorInputStream(Files.newInputStream(dumpFile.getPath()), true)) {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            saxParser.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            saxParser.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

            this.dumpFile = dumpFile;
            this.dumpHandler = new DumpSaxHandler(lang, pageIndexer);
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
                .numPagesIndexed(this.dumpHandler.getNumPagesIndexed())
                .numPagesEstimated(numPagesEstimated.get(this.dumpHandler.getLang().getCode()))
                .dumpFileName(this.dumpFile.getPath().getFileName().toString())
                .start(this.dumpHandler.getStart())
                .end(this.dumpHandler.getEnd())
                .build();
        }
    }
}
