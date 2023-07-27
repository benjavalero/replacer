package es.bvalero.replacer.admin;

import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.user.ValidateAdminUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST controller to perform administration operations */
@Tag(name = "Administration")
@Slf4j
@RestController
@RequestMapping("api/admin")
public class AdminController {

    @Autowired
    private PublicIpService publicIpService;

    @Operation(summary = "Get the public IP of the application used to perform the editions in Wikipedia")
    @ValidateAdminUser
    @GetMapping(value = "/public-ip")
    public PublicIp getPublicIp() throws ReplacerException {
        PublicIp publicIp = PublicIp.of(publicIpService.getPublicIp());
        LOGGER.info("GET Public IP: {}", publicIp);
        return publicIp;
    }
}
