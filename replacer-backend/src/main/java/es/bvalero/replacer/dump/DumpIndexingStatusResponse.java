package es.bvalero.replacer.dump;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;
import org.springframework.lang.Nullable;

@Schema(description = "Status of the current (or the last) dump indexing")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Value
@Builder
class DumpIndexingStatusResponse {

    // Even when it is quite similar to the domain object
    // we prefer to create this copy without the logic and with the different response types

    @Schema(description = "If the indexing is running", required = true, example = "false")
    boolean running;

    @Schema(description = "Number of indexable pages read", example = "251934")
    @Nullable
    Integer numPagesRead;

    @Schema(description = "Number of indexable pages indexed", example = "5016")
    @Nullable
    Integer numPagesIndexed;

    @Schema(description = "Estimated number of indexable pages", example = "249805")
    @Nullable
    Integer numPagesEstimated;

    @Schema(description = "Filename of the indexed dump", example = "glwiki-20210320-pages-articles.xml.bz2")
    @Nullable
    String dumpFileName;

    @Schema(description = "Indexing start time (in ms)", type = "number", example = "1616315965367")
    @Nullable
    Long start;

    @Schema(description = "Indexing end time (in ms)", type = "number", example = "1616317331756")
    @Nullable
    Long end;
}
