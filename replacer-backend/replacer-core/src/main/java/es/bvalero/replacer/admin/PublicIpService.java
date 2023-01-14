package es.bvalero.replacer.admin;

import es.bvalero.replacer.common.exception.ReplacerException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Service to retrieve the public IP of the application used to perform the editions in Wikipedia */
@Slf4j
@Service
class PublicIpService {

    /** Get the public IP of the application used to perform the editions in Wikipedia */
    String getPublicIp() throws ReplacerException {
        // https://stackoverflow.com/questions/8083479/java-getting-my-ip-address
        try (
            BufferedReader br = new BufferedReader(
                new InputStreamReader(new URL("http://checkip.amazonaws.com/").openStream())
            )
        ) {
            return br.readLine();
        } catch (IOException e) {
            LOGGER.error("Error getting the public IP of the tool", e);
            throw new ReplacerException(e);
        }
    }
}
