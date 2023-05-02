package es.bvalero.replacer.user;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.dto.CommonQueryParameters;
import java.util.Arrays;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ValidateUserAspect {

    private static final String LANG_HEADER_PARAM = "langHeader";

    @Autowired
    private UserRightsService userRightsService;

    @Around("@annotation(es.bvalero.replacer.user.ValidateAdminUser)")
    Object validateAdminUser(ProceedingJoinPoint joinPoint) throws Throwable {
        userRightsService.validateAdminUser(getUserId(joinPoint));
        return joinPoint.proceed();
    }

    @Around("@annotation(es.bvalero.replacer.user.ValidateBotUser)")
    Object validateBotUser(ProceedingJoinPoint joinPoint) throws Throwable {
        userRightsService.validateBotUser(getUserId(joinPoint));
        return joinPoint.proceed();
    }

    private UserId getUserId(ProceedingJoinPoint joinPoint) {
        String langHeader = getParameterByName(joinPoint, LANG_HEADER_PARAM).toString();
        WikipediaLanguage lang = WikipediaLanguage.valueOfCode(langHeader);
        CommonQueryParameters qp = findCommonQueryParameters(joinPoint);
        return UserId.of(lang, qp.getUser());
    }

    // https://stackoverflow.com/a/56459082/1264066
    private Object getParameterByName(ProceedingJoinPoint joinPoint, String parameterName) {
        MethodSignature methodSig = (MethodSignature) joinPoint.getSignature();
        Object[] args = joinPoint.getArgs();
        String[] parametersName = methodSig.getParameterNames();

        int idx = Arrays.asList(parametersName).indexOf(parameterName);
        if (args.length > idx) {
            return args[idx];
        } else {
            throw new IllegalArgumentException("No parameter with name:" + parameterName);
        }
    }

    private CommonQueryParameters findCommonQueryParameters(ProceedingJoinPoint joinPoint) {
        return Arrays
            .stream(joinPoint.getArgs())
            .filter(CommonQueryParameters.class::isInstance)
            .map(CommonQueryParameters.class::cast)
            .findAny()
            .orElseThrow(IllegalArgumentException::new);
    }
}
