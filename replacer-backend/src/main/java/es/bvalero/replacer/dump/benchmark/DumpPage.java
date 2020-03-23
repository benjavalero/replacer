package es.bvalero.replacer.dump.benchmark;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Setter
@XmlRootElement(name = "page", namespace = "http://www.mediawiki.org/xml/export-0.10/")
public class DumpPage implements Serializable {
    @XmlElement(namespace = "http://www.mediawiki.org/xml/export-0.10/")
    String title;

    @XmlElement(namespace = "http://www.mediawiki.org/xml/export-0.10/")
    int ns;

    @XmlElement(namespace = "http://www.mediawiki.org/xml/export-0.10/")
    long id;

    @XmlElement(namespace = "http://www.mediawiki.org/xml/export-0.10/")
    DumpRevision revision;
}
