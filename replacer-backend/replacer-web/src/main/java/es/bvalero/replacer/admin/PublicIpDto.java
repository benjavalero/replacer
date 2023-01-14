package es.bvalero.replacer.admin;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;
import org.springframework.lang.NonNull;

@Schema(description = "Public IP of the application used to perform the editions in Wikipedia")
@Value(staticConstructor = "of")
class PublicIpDto {

    @Schema(requiredMode = REQUIRED)
    @NonNull
    String ip;
}
