package es.bvalero.replacer.admin.publicip;

import es.bvalero.replacer.common.exception.ReplacerException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import org.springframework.stereotype.Service;

@Service
public class PublicIpService {

    /* https://stackoverflow.com/questions/8083479/java-getting-my-ip-address */
    public String getPublicIp() throws ReplacerException {
        try (
            BufferedReader br = new BufferedReader(
                new InputStreamReader(new URL("http://checkip.amazonaws.com/").openStream())
            )
        ) {
            return br.readLine();
        } catch (IOException e) {
            throw new ReplacerException(e);
        }
    }
}
