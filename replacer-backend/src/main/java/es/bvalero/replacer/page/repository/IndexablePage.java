package es.bvalero.replacer.page.repository;

import es.bvalero.replacer.domain.WikipediaLanguage;
import es.bvalero.replacer.replacement.ReplacementEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.springframework.lang.Nullable;

@Value
@Builder
public class IndexablePage {

    // TODO: There should exist a FK in DB
    @NonNull
    IndexablePageId id;

    // TODO: This should be NonNull. To check why there are still so many cases in Production DB.
    @Nullable
    String title;

    // Not retrieved from database but from Wikipedia or a dump
    // It is needed in case a page has no replacements
    @Nullable
    LocalDate lastUpdate;

    @NonNull
    List<IndexableReplacement> replacements;

    /* Named parameters to make easier the JDBC queries */

    public WikipediaLanguage getLang() {
        return this.id.getLang();
    }

    public Integer getPageId() {
        return this.id.getPageId();
    }

    // TODO: Remove once we unify with ReplacementEntity
    public List<ReplacementEntity> convert() {
        return this.getReplacements().stream().map(this::convert).collect(Collectors.toList());
    }

    private ReplacementEntity convert(IndexableReplacement replacement) {
        return ReplacementEntity
            .builder()
            .id(replacement.getId())
            .lang(this.getLang().getCode())
            .pageId(this.getPageId())
            .type(replacement.getType())
            .subtype(replacement.getSubtype())
            .position(replacement.getPosition())
            .context(replacement.getContext())
            .lastUpdate(replacement.getLastUpdate())
            .reviewer(replacement.getReviewer())
            .title(this.getTitle())
            .build();
    }
}
