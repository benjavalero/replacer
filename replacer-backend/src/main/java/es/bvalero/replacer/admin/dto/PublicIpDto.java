package es.bvalero.replacer.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;
import org.springframework.lang.NonNull;

@Schema(description = "Public IP of the application used to perform the editions in Wikipedia")
@Value(staticConstructor = "of")
public class PublicIpDto {

    @Schema(required = true)
    @NonNull
    String ip;
}
