package es.bvalero.replacer.user;

import lombok.Value;
import org.springframework.lang.NonNull;

/** OAuth access token to log in the application and be able to apply changes in Wikipedia */
@Value(staticConstructor = "of")
public class AccessToken {

    @NonNull
    String token;

    @NonNull
    String tokenSecret;
}
