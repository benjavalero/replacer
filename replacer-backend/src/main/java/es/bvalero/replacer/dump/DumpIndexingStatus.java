package es.bvalero.replacer.dump;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Value;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Schema(description = "Status of the current (or the last) dump indexing")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Value
@Builder
class DumpIndexingStatus {

    @Schema(description = "If the indexing is running", required = true, example = "false")
    @NonNull
    Boolean running;

    @Schema(description = "Number of indexable pages read", example = "251934")
    @Nullable
    Long numPagesRead;

    @Schema(description = "Number of indexable pages indexed", example = "5016")
    @Nullable
    Long numPagesIndexed;

    @Schema(description = "Estimated number of indexable pages", example = "249805")
    @Nullable
    Long numPagesEstimated;

    @Schema(description = "Filename of the indexed dump", example = "glwiki-20210320-pages-articles.xml.bz2")
    @Nullable
    String dumpFileName;

    @Schema(description = "Indexing start time (in ms)", type = "number", example = "1616315965367")
    @Nullable
    @JsonSerialize(using = DumpLocalDateTimeSerializer.class)
    LocalDateTime start;

    @Schema(description = "Indexing end time (in ms)", type = "number", example = "1616317331756")
    @Nullable
    @JsonSerialize(using = DumpLocalDateTimeSerializer.class)
    LocalDateTime end;

    static DumpIndexingStatus ofEmpty() {
        return DumpIndexingStatus.builder().running(false).build();
    }
}
