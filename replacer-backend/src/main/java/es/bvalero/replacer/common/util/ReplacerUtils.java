package es.bvalero.replacer.common.util;

import es.bvalero.replacer.common.exception.ReplacerException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Locale;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ReplacerUtils {

    public static final Locale LOCALE_ES = Locale.forLanguageTag("es");

    public String getContextAroundWord(String text, int start, int end, int threshold) {
        int limitLeft = Math.max(0, start - threshold);
        int limitRight = Math.min(text.length(), end + threshold);
        return text.substring(limitLeft, limitRight);
    }

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
