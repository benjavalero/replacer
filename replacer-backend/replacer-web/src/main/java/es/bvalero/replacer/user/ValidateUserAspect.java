package es.bvalero.replacer.user;

import es.bvalero.replacer.common.util.WebUtils;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
public class ValidateUserAspect {

    @Autowired
    private WebUtils webUtils;

    @Around("@annotation(es.bvalero.replacer.user.ValidateAdminUser)")
    Object validateAdminUser(ProceedingJoinPoint joinPoint) throws Throwable {
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

    @Around("@annotation(es.bvalero.replacer.user.ValidateBotUser)")
    Object validateBotUser(ProceedingJoinPoint joinPoint) throws Throwable {
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
