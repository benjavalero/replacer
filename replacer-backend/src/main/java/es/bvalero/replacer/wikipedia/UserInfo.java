package es.bvalero.replacer.wikipedia;

import java.util.List;
import lombok.Value;

@Value(staticConstructor = "of")
public class UserInfo {

    String name;
    List<String> groups;
}
