package es.bvalero.replacer.dump;

import lombok.*;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Setter
@XmlRootElement(name = "revision", namespace = "http://www.mediawiki.org/xml/export-0.10/")
public class DumpRevision implements Serializable {
    @XmlElement(namespace = "http://www.mediawiki.org/xml/export-0.10/")
    String timestamp;

    @XmlElement(namespace = "http://www.mediawiki.org/xml/export-0.10/")
    String text;
}
