package es.bvalero.replacer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/* https://stackoverflow.com/questions/8083479/java-getting-my-ip-address */
@Deprecated(forRemoval = true)
@Slf4j
@Component
public class MyIpFinder {

    @PostConstruct
    public void logMyIP() {
        LOGGER.error("{} - {}", findIpWithAmazon(), findIpWithIcanhazip());
    }

    @Nullable
    private String findIpWithAmazon() {
        try {
            URL url = new URL("http://checkip.amazonaws.com/");
            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
            return br.readLine();
        } catch (IOException e) {
            return null;
        }
    }

    @Nullable
    private String findIpWithIcanhazip() {
        try {
            URL myIP = new URL("http://icanhazip.com/");
            BufferedReader in = new BufferedReader(new InputStreamReader(myIP.openStream()));
            return in.readLine();
        } catch (IOException e) {
            return null;
        }
    }
}
