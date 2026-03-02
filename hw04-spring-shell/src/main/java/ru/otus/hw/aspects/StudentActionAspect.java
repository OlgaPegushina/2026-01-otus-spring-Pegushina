package ru.otus.hw.aspects;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import ru.otus.hw.domain.Student;
import ru.otus.hw.service.LocalizedIOService;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StudentActionAspect {

    private final LocalizedIOService ioService;

    // -- любой метод в пакете service
    @Pointcut("within(ru.otus.hw.service..*)")
    public void anyServiceMethod() {
    }

    // -- метод регистрации студента
    @Pointcut("execution(* ru.otus.hw.service.StudentService.determineCurrentStudent(..))")
    public void studentLoginMethod() {
    }

    @AfterReturning(value = "studentLoginMethod()", returning = "student")
    public void logStudentLogin(JoinPoint joinPoint, Student student) {
        log.info(ioService.getMessage("Student.aspect.login",
                student.firstName(), student.lastName()));
    }

    @AfterThrowing(value = "anyServiceMethod()", throwing = "exception")
    public void logServiceException(JoinPoint joinPoint, Exception exception) {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        log.error(ioService.getMessage("Student.aspect.error",
                className, methodName, exception.getMessage()));
    }
}
