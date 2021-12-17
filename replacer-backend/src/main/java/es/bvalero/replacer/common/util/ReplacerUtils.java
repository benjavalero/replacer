package es.bvalero.replacer.common.util;

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
}
