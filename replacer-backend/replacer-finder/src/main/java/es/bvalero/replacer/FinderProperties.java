package es.bvalero.replacer;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.lang.Nullable;

@Configuration
@ConfigurationProperties(prefix = "replacer.finder")
@PropertySource(value = "classpath:application-finder.yml", factory = YamlPropertySourceFactory.class)
@Data
public class FinderProperties {

    // Listings
    private Map<String, String> simpleMisspellingPages;
    private Map<String, String> composedMisspellingPages;
    private Map<String, String> falsePositivePages;

    // Spaces
    private Map<String, String> templateWords;
    private Map<String, List<String>> fileWords;
    private Map<String, List<String>> imageWords;
    private Map<String, List<String>> annexWords;
    private Map<String, List<String>> categoryWords;

    public Set<String> getAllFileWords() {
        return this.fileWords.values().stream().flatMap(List::stream).collect(Collectors.toUnmodifiableSet());
    }

    public Set<String> getAllImageWords() {
        return this.imageWords.values().stream().flatMap(List::stream).collect(Collectors.toUnmodifiableSet());
    }

    public Set<String> getAllAnnexWords() {
        return this.annexWords.values().stream().flatMap(List::stream).collect(Collectors.toUnmodifiableSet());
    }

    public Set<String> getAllCategoryWords() {
        return this.categoryWords.values().stream().flatMap(List::stream).collect(Collectors.toUnmodifiableSet());
    }

    public Set<String> getAllSpaceWords() {
        return Stream
            .of(getAllFileWords(), getAllImageWords(), getAllAnnexWords(), getAllCategoryWords())
            .flatMap(Collection::stream)
            .collect(Collectors.toUnmodifiableSet());
    }

    // Names
    private List<String> personNames;
    private List<PersonSurname> personSurnames;

    @Data
    public static class PersonSurname {

        private String surname;
        private boolean ignoreName = false;
    }

    // Ignore
    private List<String> redirectionWords; // A list to process them with the common case first
    private Set<String> ignorableTemplates;
    private Set<String> ignorableSections;
    private Set<String> completeTags;

    // Template Parameters
    private List<TemplateParam> templateParams;

    @Data
    public static class TemplateParam {

        @Nullable
        private String template;

        private boolean partial = false;

        @Nullable
        private String param;
    }

    // Dates

    private Map<String, List<String>> monthNames;
    private Map<String, Set<String>> dateConnectors;
    private Map<String, List<String>> yearPrepositions;
    private Map<String, Set<DateArticle>> dateArticles;

    @Data
    public static class DateArticle {

        private String prep;
        private String article;
    }

    // Ordinals

    private Map<String, Map<Integer, OrdinalSuggestion>> ordinalSuggestions;
    private OrdinalOption ordinalSuffixes;

    @Data
    public static class OrdinalSuggestion {

        private OrdinalOption ordinal;
        private OrdinalOption fractional;
    }

    @Data
    public static class OrdinalOption {

        private List<String> masculine;
        private List<String> feminine;
    }
}
