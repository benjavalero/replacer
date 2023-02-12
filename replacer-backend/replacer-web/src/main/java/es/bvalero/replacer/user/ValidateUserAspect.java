package es.bvalero.replacer.user;

import es.bvalero.replacer.common.dto.CommonQueryParameters;
import java.util.Arrays;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ValidateUserAspect {

    @Autowired
    private UserRightsService userRightsService;

    @Around("@annotation(es.bvalero.replacer.user.ValidateAdminUser)")
    Object validateAdminUser(ProceedingJoinPoint joinPoint) throws Throwable {
        CommonQueryParameters qp = findCommonQueryParameters(joinPoint);
        userRightsService.validateAdminUser(qp.getUserId());
        return joinPoint.proceed();
    }

    @Around("@annotation(es.bvalero.replacer.user.ValidateBotUser)")
    Object validateBotUser(ProceedingJoinPoint joinPoint) throws Throwable {
        CommonQueryParameters qp = findCommonQueryParameters(joinPoint);
        userRightsService.validateBotUser(qp.getUserId());
        return joinPoint.proceed();
    }

    private CommonQueryParameters findCommonQueryParameters(ProceedingJoinPoint joinPoint) {
        return Arrays
            .stream(joinPoint.getArgs())
            .filter(CommonQueryParameters.class::isInstance)
            .map(CommonQueryParameters.class::cast)
            .findAny().orElseThrow(IllegalArgumentException::new);
    }
}
