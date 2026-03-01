package ru.otus.hw.shell;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import ru.otus.hw.domain.Student;
import ru.otus.hw.security.LoginContext;
import ru.otus.hw.service.LocalizedIOService;
import ru.otus.hw.service.StudentService;
import ru.otus.hw.service.TestRunnerService;

@ShellComponent(value = "Commands for the console")
@RequiredArgsConstructor
public class ShellTestCommands {

    private final TestRunnerService testRunnerService;

    private final StudentService studentService;

    private final LoginContext loginContext;

    private final ConfigurableApplicationContext context;

    private final LocalizedIOService ioService;

    private Student student;

    @ShellMethod(value = "Login command", key = {"login", "l"})
    public void login() {
        student = studentService.determineCurrentStudent();
        String fullName = student.getFullName();
        loginContext.login(fullName);
        ioService.printFormattedLineLocalized("ShellTestCommands.greeting", fullName);
    }

    @ShellMethod(value = "Start test", key = {"start", "s"})
    @ShellMethodAvailability(value = "testingAvailability")
    public void startTestingStudents() {
        testRunnerService.runTest(student);
    }

    @ShellMethod(value = "Quit", key = {"q", "quit"})
    public void endTest() {
        ioService.printLineLocalized("ShellTestCommands.end");
        SpringApplication.exit(context, () -> 0);
        System.exit(0);
    }

    private Availability testingAvailability() {
        return loginContext.isUserLoggedIn()
                ? Availability.available()
                : Availability.unavailable(ioService.getMessage("ShellTestCommands.login"));
    }
}
