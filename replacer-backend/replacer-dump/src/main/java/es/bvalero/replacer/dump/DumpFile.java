package es.bvalero.replacer.dump;

import java.nio.file.Path;
import lombok.Value;

@Value(staticConstructor = "of")
class DumpFile {

    Path path;
}
