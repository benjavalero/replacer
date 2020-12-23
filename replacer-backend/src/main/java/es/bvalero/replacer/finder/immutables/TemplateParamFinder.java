package es.bvalero.replacer.finder.immutables;

import es.bvalero.replacer.finder.*;
import es.bvalero.replacer.page.IndexablePage;
import java.util.*;
import java.util.regex.MatchResult;
import javax.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Find template parameters, e.g. `param` in `{{Template|param=value}}`.
 * For some specific parameters (see `template-param.xml`), we include in the result also the value,
 * which is usually a taxonomy, a Commons category, etc.
 * Finally, we include also the value if it seems like a file or a domain.
 */
@Component
public class TemplateParamFinder implements ImmutableFinder {

    private static final Set<Character> ALLOWED_CHARS = new HashSet<>(Arrays.asList('-', '_'));
    private static final Set<Character> END_CHARS = new HashSet<>(Arrays.asList('|', '}', ']', '<', '{'));

    @Resource
    private List<String> paramNames;

    @Resource
    private List<String> paramValues;

    @Override
    public ImmutableFinderPriority getPriority() {
        return ImmutableFinderPriority.HIGH;
    }

    @Override
    public int getMaxLength() {
        return 1000;
    }

    @Override
    public Iterable<Immutable> find(IndexablePage page) {
        return new LinearIterable<>(page, this::findResult, this::convert);
    }

    @Nullable
    public MatchResult findResult(IndexablePage page, int start) {
        List<MatchResult> matches = new ArrayList<>(100);
        while (start >= 0 && start < page.getContent().length() && matches.isEmpty()) {
            start = findTemplateParam(page.getContent(), start, matches);
        }
        return matches.isEmpty() ? null : matches.get(0);
    }

    private int findTemplateParam(String text, int start, List<MatchResult> matches) {
        int startMatch = findStartParam(text, start);
        if (startMatch >= 0) {
            int startParam = startMatch + 1;
            int posEquals = findPosEquals(text, startParam);
            if (posEquals >= 0) {
                String param = text.substring(startParam, posEquals);
                if (isParamValid(param)) {
                    int endParam = findEndParam(text, posEquals + 1);
                    if (endParam >= 0) {
                        String value = text.substring(posEquals + 1, endParam).trim();
                        if (paramNames.contains(param.trim())) {
                            // If the parameter is known then return the complete match
                            matches.add(LinearMatcher.of(startParam, text.substring(startParam, endParam)));
                        } else if (paramValues.contains(value)) {
                            // If the value is known then return the complete match
                            matches.add(LinearMatcher.of(startParam, text.substring(startParam, endParam)));
                        } else if (matchesFile(value)) {
                            // If the value is a file or a domain then return the complete match
                            matches.add(LinearMatcher.of(startParam, text.substring(startParam, endParam)));
                        } else if (!matchesAttribute(value)) {
                            // If the value is an XML attribute then skip. Values with quotes inside are admitted.
                            // In the rest of cases return only the param
                            matches.add(LinearMatcher.of(startParam, param));
                        }
                        return endParam + 1;
                    } else {
                        return posEquals + 1;
                    }
                } else {
                    return startParam;
                }
            } else {
                return startParam;
            }
        } else {
            return -1;
        }
    }

    private int findStartParam(String text, int start) {
        return text.indexOf('|', start);
    }

    private int findPosEquals(String text, int start) {
        return text.indexOf('=', start);
    }

    private int findEndParam(String text, int start) {
        for (int i = start; i < text.length(); i++) {
            if (END_CHARS.contains(text.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

    private boolean isParamValid(String param) {
        if (StringUtils.isBlank(param)) {
            return false;
        }

        // The start of the parameter '|' could be followed by a dash '-' in tables
        if (param.startsWith("-")) {
            return false;
        }

        for (int i = 0; i < param.length(); i++) {
            if (!isParamChar(param.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private boolean isParamChar(char ch) {
        return Character.isLetterOrDigit(ch) || Character.isWhitespace(ch) || ALLOWED_CHARS.contains(ch);
    }

    private boolean matchesFile(String value) {
        int dot = value.lastIndexOf('.');
        if (dot >= 0) {
            String extension = value.substring(dot + 1);
            return extension.length() >= 2 && extension.length() <= 4 && FinderUtils.isAsciiLowercase(extension);
        } else {
            return false;
        }
    }

    private boolean matchesAttribute(String value) {
        return value.startsWith("\"");
    }
}
