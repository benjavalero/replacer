package es.bvalero.replacer.dump.benchmark;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.core.io.InputStreamResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DumpReader extends StaxEventItemReader<DumpPage> {

    public DumpReader(Path path) {
        super();

        try {
            setResource(new InputStreamResource(new BZip2CompressorInputStream(Files.newInputStream(path), true)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        setFragmentRootElementName("{http://www.mediawiki.org/xml/export-0.10/}page");

        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(DumpPage.class);
        setUnmarshaller(marshaller);
    }
}
