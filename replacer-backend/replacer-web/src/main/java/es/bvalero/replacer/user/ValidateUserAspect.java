package es.bvalero.replacer.user;

import es.bvalero.replacer.common.security.ForbiddenException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
public class ValidateUserAspect {

    // Dependency injection
    private final WebUtils webUtils;

    public ValidateUserAspect(WebUtils webUtils) {
        this.webUtils = webUtils;
    }

    @Around("@annotation(es.bvalero.replacer.common.security.ValidateAdminUser)")
    public Object validateAdminUser(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request =
            ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        User user = webUtils.getAuthenticatedUser(request);
        if (!user.isAdmin()) {
            LOGGER.error(
                "User {} is not an administrator and is accessing method {}",
                user,
                joinPoint.getSignature().getName()
            );
            throw new ForbiddenException();
        }
        return joinPoint.proceed();
    }

    @Around("@annotation(es.bvalero.replacer.common.security.ValidateBotUser)")
    public Object validateBotUser(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request =
            ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        User user = webUtils.getAuthenticatedUser(request);
        if (!user.isBot()) {
            LOGGER.error("User {} is not a bot and is accessing method {}", user, joinPoint.getSignature().getName());
            throw new ForbiddenException();
        }
        return joinPoint.proceed();
    }
}
