package org.haz.service;

import org.haz.audit.AuditLogger;
import org.haz.model.Employee;
import org.haz.notify.NotificationManager;
import org.haz.repository.EmployeeRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmployeeServiceImpl implements EmployeeService {
    private final EmployeeRepository repository;
    private final NotificationManager notificationManager;

    private final ObjectProvider<AuditLogger> auditLoggerProvider;

    private EmployeeValidator validator;

    @Value("${company.name}")
    private String companyName;

    @Value("${company.currency}")
    private String currency;

    @Value("${raise.max-percentage}")
    private double maxRaisePercentage;

    public EmployeeServiceImpl(EmployeeRepository repository,
                               NotificationManager notificationManager,
                               ObjectProvider<AuditLogger> auditLoggerProvider) {
        this.repository = repository;
        this.notificationManager = notificationManager;
        this.auditLoggerProvider = auditLoggerProvider;
    }

    @Autowired
    public void setValidator(EmployeeValidator validator) {
        this.validator = validator;
    }

    @PostConstruct
    public void init() {
        System.out.println("[LIFECYCLE] EmployeeServiceImpl initialised for " + companyName
                + ", repository in use: " + repository.getClass().getSimpleName());
    }

    @Override
    public void addEmployee(Employee employee) {
        validator.validate(employee);
        repository.save(employee);
        auditLoggerProvider.getObject().log("employee added: " + employee.getName());
        notificationManager.notifyAll("New employee joined " + companyName + ": "
                + employee.getName() + " (" + employee.getDepartment() + ")");
    }

    @Override
    public Optional<Employee> getEmployeeById(int id) {
        return repository.findById(id);
    }

    @Override
    public List<Employee> getAllEmployees() {
        return repository.findAll();
    }

    @Override
    public double giveRaise(int id, double percentage) {
        if (percentage <= 0) {
            throw new InvalidEmployeeException("raise percentage must be positive");
        }

        if (percentage > maxRaisePercentage) {
            throw new InvalidEmployeeException("raise of " + percentage
                    + "% exceeds the company limit of " + maxRaisePercentage + "%");
        }

        Employee employee = repository.findById(id)
                .orElseThrow(() -> new InvalidEmployeeException("no employee with id " + id));

        double oldSalary = employee.getSalary();
        employee.setSalary(oldSalary + (oldSalary * percentage / 100));
        validator.validate(employee);
        repository.save(employee);

        auditLoggerProvider.getObject().log("raise of " + percentage + "% applied to " + employee.getName());
        notificationManager.notifyAll(String.format("%s received a %.1f%% raise: %.2f -> %.2f %s",
                employee.getName(), percentage, oldSalary, employee.getSalary(), currency));
        notificationManager.notifyUrgent("payroll must be updated for " + employee.getName());

        return employee.getSalary();
    }

    @Override
    public String getCompanyName() {
        return companyName;
    }

    @Override
    public String getCurrency() {
        return currency;
    }

    @Override
    public double getMaxRaisePercentage() {
        return maxRaisePercentage;
    }

    @PreDestroy
    public void shutdown() {
        System.out.println("[LIFECYCLE] EmployeeServiceImpl destroyed");
    }
}
