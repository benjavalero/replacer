package es.bvalero.replacer.user;

import es.bvalero.replacer.common.dto.CommonQueryParameters;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Aspect
@Component
public class ValidateUserAspect {

    @Autowired
    private UserRightsService userRightsService;

    @Around("@annotation(ValidateAdminUser)")
    Object validateAdminUser(ProceedingJoinPoint joinPoint) throws Throwable {
        findCommonQueryParameters(joinPoint).ifPresentOrElse(qp -> userRightsService.validateAdminUser(qp.getWikipediaLanguage(), qp.getUser()),
            () -> {throw new IllegalArgumentException("Expected CommonQueryParameters argument"); });

        return joinPoint.proceed();
    }

    @Around("@annotation(ValidateBotUser)")
    Object validateBotUser(ProceedingJoinPoint joinPoint) throws Throwable {
        findCommonQueryParameters(joinPoint).ifPresentOrElse(qp -> userRightsService.validateBotUser(qp.getWikipediaLanguage(), qp.getUser()),
            () -> {throw new IllegalArgumentException("Expected CommonQueryParameters argument"); });

        return joinPoint.proceed();
    }

    private Optional<CommonQueryParameters> findCommonQueryParameters(ProceedingJoinPoint joinPoint) {
        return             Arrays.stream(joinPoint.getArgs()).filter(CommonQueryParameters.class::isInstance)
                .map(CommonQueryParameters.class::cast)
                .findAny();
    }
}
