package es.bvalero.replacer.page.save;

import static org.junit.jupiter.api.Assertions.assertThrows;

import es.bvalero.replacer.user.User;
import es.bvalero.replacer.wikipedia.WikipediaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EditionsPerMinuteValidatorTest {

    private static final int MAX_EDITIONS_PER_MINUTE = 5;

    private EditionsPerMinuteValidator editionsPerMinuteValidator;

    @BeforeEach
    public void setUp() {
        editionsPerMinuteValidator = new EditionsPerMinuteValidator();
        editionsPerMinuteValidator.setMaxEditionsPerMinute(MAX_EDITIONS_PER_MINUTE);
    }

    @Test
    void testMaximumEditionsPerMinute() throws WikipediaException {
        User user = User.buildTestUser();

        for (int i = 0; i < MAX_EDITIONS_PER_MINUTE; i++) {
            editionsPerMinuteValidator.validate(user);
        }

        assertThrows(WikipediaException.class, () -> editionsPerMinuteValidator.validate(user));
    }
}
