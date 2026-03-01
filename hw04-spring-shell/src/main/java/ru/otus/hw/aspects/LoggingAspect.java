package ru.otus.hw.aspects;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.otus.hw.service.LocalizedIOService;

import java.util.Arrays;

@Aspect
@Component
@RequiredArgsConstructor
public class LoggingAspect {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final LocalizedIOService ioService;

    @Around("@annotation(ru.otus.hw.annotation.Loggable)")
    public Object logTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        logger.info(ioService.getMessage("Logging.aspect.call", methodName, Arrays.toString(args)));

        Object proceed;
        try {
            // -- выполняется сам метод
            proceed = joinPoint.proceed();
        } catch (Throwable e) {
            logger.error(ioService.getMessage("Logging.aspect.error", methodName, e.getMessage()));
            throw e;
        }

        long executionTime = System.currentTimeMillis() - startTime;
        logger.info(ioService.getMessage("Logging.aspect.time", methodName, executionTime));

        return proceed;
    }
}