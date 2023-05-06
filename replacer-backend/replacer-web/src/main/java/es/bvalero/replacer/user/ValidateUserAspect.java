package es.bvalero.replacer.user;

import es.bvalero.replacer.common.exception.ForbiddenException;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class ValidateUserAspect {

    @Around("@annotation(es.bvalero.replacer.user.ValidateAdminUser)")
    Object validateAdminUser(ProceedingJoinPoint joinPoint) throws Throwable {
        User user = getUser(joinPoint);
        if (!user.isAdmin()) {
            LOGGER.error("Unauthorized admin user: {}", user);
            throw new ForbiddenException();
        }
        return joinPoint.proceed();
    }

    @Around("@annotation(es.bvalero.replacer.user.ValidateBotUser)")
    Object validateBotUser(ProceedingJoinPoint joinPoint) throws Throwable {
        User user = getUser(joinPoint);
        if (!user.isBot()) {
            LOGGER.error("Unauthorized bot user: {}", user);
            throw new ForbiddenException();
        }
        return joinPoint.proceed();
    }

    private User getUser(ProceedingJoinPoint joinPoint) {
        return Arrays
            .stream(joinPoint.getArgs())
            .filter(User.class::isInstance)
            .map(User.class::cast)
            .findAny()
            .orElseThrow(IllegalArgumentException::new);
    }
}
