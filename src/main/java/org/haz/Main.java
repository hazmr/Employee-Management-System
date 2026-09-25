package org.haz;

import org.haz.audit.AuditLogger;
import org.haz.config.AppConfig;
import org.haz.model.Employee;
import org.haz.service.EmployeeService;
import org.haz.service.InvalidEmployeeException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {
    public static void main(String[] args) {
        String profile = args.length > 0 ? args[0] : "dev";

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.getEnvironment().setActiveProfiles(profile);
        context.register(AppConfig.class);

        section("STARTING CONTEXT WITH PROFILE: " + profile);
        context.refresh();

        EmployeeService service = context.getBean(EmployeeService.class);

        section("1. ADD A VALID EMPLOYEE");
        service.addEmployee(new Employee(1, "Mona Adel", "Engineering", 12000));
        service.addEmployee(new Employee(2, "Karim Fouad", "Sales", 9000));

        section("2. ADD AN INVALID EMPLOYEE");
        try {
            service.addEmployee(new Employee(3, "   ", "Support", 7000));
        } catch (InvalidEmployeeException e) {
            System.out.println("   rejected: " + e.getMessage());
        }
        try {
            service.addEmployee(new Employee(4, "Nour Hassan", "Support", -500));
        } catch (InvalidEmployeeException e) {
            System.out.println("   rejected: " + e.getMessage());
        }

        section("3. GIVE A RAISE WITHIN THE LIMIT (10%)");
        double newSalary = service.giveRaise(1, 10);
        System.out.println("   new salary: " + newSalary + " " + service.getCurrency());

        section("4. GIVE A RAISE OVER THE LIMIT (50%)");
        try {
            service.giveRaise(2, 50);
        } catch (InvalidEmployeeException e) {
            System.out.println("   rejected: " + e.getMessage());
        }

        section("5. SINGLETON VS PROTOTYPE");
        EmployeeService serviceAgain = context.getBean(EmployeeService.class);
        System.out.println("   same EmployeeService instance twice? "
                + (service == serviceAgain) + " (singleton)");

        AuditLogger first = context.getBean(AuditLogger.class);
        AuditLogger second = context.getBean(AuditLogger.class);
        AuditLogger third = context.getBean(AuditLogger.class);
        first.log("manual audit entry");
        second.log("manual audit entry");
        third.log("manual audit entry");
        System.out.println("   audit logger ids: " + first.getInstanceId() + ", "
                + second.getInstanceId() + ", " + third.getInstanceId());
        System.out.println("   same AuditLogger instance twice? "
                + (first == second) + " (prototype)");

        section("6. ALL EMPLOYEES");
        service.getAllEmployees().forEach(e -> System.out.println("   " + e));
        System.out.println("   lookup by id 2: " + service.getEmployeeById(2).orElse(null));

        section("7. EXTERNALISED PROPERTIES");
        System.out.println("   company.name        = " + service.getCompanyName());
        System.out.println("   company.currency    = " + service.getCurrency());
        System.out.println("   raise.max-percentage= " + service.getMaxRaisePercentage());
        System.out.println("   notification.retry-count= "
                + context.getBean(org.haz.notify.NotificationManager.class).getRetryCount());

        section("8. CLOSING CONTEXT");

        context.close();
    }

    private static void section(String title) {
        System.out.println();
        System.out.println("=== " + title + " ===");
    }
}
