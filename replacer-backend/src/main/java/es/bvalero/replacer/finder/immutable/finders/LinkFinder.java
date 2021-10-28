package es.bvalero.replacer.finder.immutable.finders;

import static es.bvalero.replacer.finder.util.LinkUtils.END_LINK;
import static es.bvalero.replacer.finder.util.LinkUtils.START_LINK;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.immutable.Immutable;
import es.bvalero.replacer.finder.immutable.ImmutableCheckedFinder;
import es.bvalero.replacer.finder.immutable.ImmutableFinderPriority;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.LinearMatchResult;
import es.bvalero.replacer.finder.util.LinkUtils;
import java.util.*;
import java.util.regex.MatchResult;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Find link-related immutables:
 * <ul>
 *     <li>Find categories, e.g. `[[Categoría:España]]`</li>
 *     <li>Find links with suffix, e.g. `[[brasil]]eño`</li>
 *     <li>Find the first part of aliased links, e.g. `brasil` in `[[brasil|Brasil]]`. It also finds files.</li>
 *     <li>Find inter-language links, e.g. `[[pt:Title]]`</li>
 *     <li>Find filenames, e.g. `xx.jpg` in `[[File:xx.jpg]]`</li>
 * </ul>
 */
@Component
class LinkFinder extends ImmutableCheckedFinder {

    private static final String FILE_SPACE_EN = "File";
    private static final String IMAGE_SPACE_EN = "Image";
    private static final String CATEGORY_SPACE_EN = "Category";

    @Resource
    private Map<String, String> fileWords;

    @Resource
    private Map<String, String> imageWords;

    @Resource
    private Map<String, String> categoryWords;

    private final Set<String> fileSpaces = new HashSet<>();
    private final Set<String> categorySpaces = new HashSet<>();

    @PostConstruct
    public void init() {
        this.fileSpaces.add(FILE_SPACE_EN);
        this.fileSpaces.addAll(
                fileWords
                    .values()
                    .stream()
                    .flatMap(val -> Arrays.stream(StringUtils.split(val)))
                    .collect(Collectors.toList())
            );
        this.fileSpaces.add(IMAGE_SPACE_EN);
        this.fileSpaces.addAll(imageWords.values());

        this.categorySpaces.add(CATEGORY_SPACE_EN);
        this.categorySpaces.addAll(categoryWords.values());
    }

    @Override
    public ImmutableFinderPriority getPriority() {
        return ImmutableFinderPriority.LOW;
    }

    @Override
    public int getMaxLength() {
        return 250;
    }

    @Override
    public Iterable<Immutable> find(FinderPage page) {
        List<Immutable> immutables = new ArrayList<>(100);
        for (LinearMatchResult template : LinkUtils.findAllLinks(page)) {
            immutables.addAll(findImmutables(template, page));
        }
        return immutables;
    }

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        // We are overriding the more general find method
        throw new IllegalCallerException();
    }

    private List<Immutable> findImmutables(LinearMatchResult link, FinderPage page) {
        // If the link is suffixed then return the complete link
        String text = page.getContent();
        int endSuffix = findEndSuffix(text, link.end());
        if (endSuffix > link.end()) {
            return Collections.singletonList(Immutable.of(link.start(), text.substring(link.start(), endSuffix)));
        }

        String content = link.group().substring(START_LINK.length(), link.group().length() - END_LINK.length());

        int posPipe = content.lastIndexOf('|');
        String linkTitle = posPipe >= 0 ? content.substring(0, posPipe) : content;
        String linkAlias = posPipe >= 0 ? content.substring(posPipe + 1) : null;

        int posColon = linkTitle.indexOf(':');
        String linkSpace = posColon >= 0 ? linkTitle.substring(0, posColon) : null;

        // If the link space is in the list then return an immutable of the complete link
        if (isCategorySpace(linkSpace)) {
            return Collections.singletonList(this.convert(link));
        }

        // If the link alias doesn't exist and the link is in file/lang space then return the complete link
        if (linkAlias == null && (isFileSpace(linkSpace) || isLangSpace(linkSpace) || isInterWikiSpace(linkSpace))) {
            return Collections.singletonList(this.convert(link));
        }

        // If the link alias exists then return the link title
        // In case the alias exists but is a parameter then return the complete link
        if (linkAlias != null) {
            if (linkAlias.contains("=")) {
                return Collections.singletonList(this.convert(link));
            } else {
                return Collections.singletonList(Immutable.of(link.start() + START_LINK.length(), linkTitle));
            }
        }

        // In any other case then return no immutable
        return Collections.emptyList();
    }

    private int findEndSuffix(String text, int start) {
        for (int i = start; i < text.length(); i++) {
            if (!Character.isLowerCase(text.charAt(i))) {
                return i;
            }
        }
        // In case we reach the end of the text
        return text.length();
    }

    private boolean isCategorySpace(@Nullable String space) {
        return space != null && categorySpaces.contains(FinderUtils.setFirstUpperCase(space.trim()));
    }

    private boolean isFileSpace(@Nullable String space) {
        return space != null && fileSpaces.contains(FinderUtils.setFirstUpperCase(space.trim()));
    }

    private boolean isLangSpace(@Nullable String space) {
        return space != null && space.trim().length() == 2 && FinderUtils.isAsciiLowercase(space.trim());
    }

    private boolean isInterWikiSpace(@Nullable String space) {
        return space != null && space.trim().length() == 0;
    }
}
