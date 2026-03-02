package ru.otus.hw.aspects;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import ru.otus.hw.service.LocalizedIOService;

import java.util.Arrays;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LoggingAspect {

    private final LocalizedIOService ioService;

    @Around("@annotation(ru.otus.hw.annotation.Loggable)")
    public Object logTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        log.info(ioService.getMessage("Logging.aspect.call", methodName, Arrays.toString(args)));

        Object proceed;
        try {
            // -- выполняется сам метод
            proceed = joinPoint.proceed();
        } catch (Throwable e) {
            log.error(ioService.getMessage("Logging.aspect.error", methodName, e.getMessage()));
            throw e;
        }

        long executionTime = System.currentTimeMillis() - startTime;
        log.info(ioService.getMessage("Logging.aspect.time", methodName, executionTime));

        return proceed;
    }
}