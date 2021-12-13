package es.bvalero.replacer.authentication.useradmin;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CheckUserAdminServiceTest {

    private CheckUserAdminService checkUserAdminService;

    @BeforeEach
    public void setUp() {
        checkUserAdminService = new CheckUserAdminService();
        checkUserAdminService.setAdminUser("X");
    }

    @Test
    void testIsAdminUser() {
        assertTrue(checkUserAdminService.isAdminUser("X"));
        assertFalse(checkUserAdminService.isAdminUser("Z"));
    }
}
