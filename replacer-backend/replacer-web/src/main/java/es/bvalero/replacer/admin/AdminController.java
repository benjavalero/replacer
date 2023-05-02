package es.bvalero.replacer.admin;

import com.github.rozidan.springboot.logger.Loggable;
import es.bvalero.replacer.common.dto.CommonQueryParameters;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.user.ValidateAdminUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST controller to perform administration operations */
@Tag(name = "Administration")
@Loggable
@RestController
@RequestMapping("api/admin")
public class AdminController {

    @Autowired
    private PublicIpService publicIpService;

    @Operation(summary = "Get the public IP of the application used to perform the editions in Wikipedia")
    @ValidateAdminUser
    @GetMapping(value = "/public-ip")
    public PublicIp getPublicIp(
        @RequestHeader(HttpHeaders.ACCEPT_LANGUAGE) String langHeader,
        @Valid CommonQueryParameters queryParameters
    ) throws ReplacerException {
        return PublicIp.of(publicIpService.getPublicIp());
    }
}
