package es.bvalero.replacer.authentication.useradmin;

import lombok.Setter;
import org.jetbrains.annotations.TestOnly;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CheckUserAdminService {

    @Setter(onMethod_ = @TestOnly)
    @Value("${replacer.admin.user}")
    private String adminUser;

    public boolean isAdminUser(String username) {
        return this.adminUser.equals(username);
    }
}
