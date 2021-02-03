package es.bvalero.replacer.dump;

import com.jcabi.aspects.Loggable;
import es.bvalero.replacer.ReplacerException;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.annotation.PostConstruct;
import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

@Component
class DumpJobSaxImpl implements DumpJob {

    @Autowired
    private DumpHandler dumpHandler;

    @PostConstruct
    void setProperty() {
        System.setProperty("jdk.xml.totalEntitySizeLimit", Integer.toString(0));
    }

    @Override
    @Loggable(prepend = true, value = Loggable.DEBUG)
    public void parseDumpFile(Path dumpFile, WikipediaLanguage lang) throws ReplacerException {
        try (InputStream xmlInput = new BZip2CompressorInputStream(Files.newInputStream(dumpFile), true)) {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            saxParser.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            saxParser.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

            dumpHandler.setLatestDumpFile(dumpFile);
            dumpHandler.setLang(lang);
            saxParser.parse(xmlInput, dumpHandler);
        } catch (IOException e) {
            throw new ReplacerException("Dump file not valid", e);
        } catch (ParserConfigurationException | SAXException e) {
            throw new ReplacerException("SAX Error parsing dump file", e);
        }
    }

    @Override
    public DumpIndexingStatus getDumpIndexingStatus() {
        return dumpHandler.getDumpIndexingStatus();
    }

    @Override
    public boolean isRunning() {
        return getDumpIndexingStatus().isRunning();
    }
}
