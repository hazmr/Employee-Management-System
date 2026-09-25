package org.haz.service;

import org.haz.model.Employee;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Component
public class EmployeeValidator implements InitializingBean, DisposableBean {
    public void validate(Employee employee) {
        if (employee == null) {
            throw new InvalidEmployeeException("employee must not be null");
        }
        if (employee.getName() == null || employee.getName().isBlank()) {
            throw new InvalidEmployeeException("employee name must not be blank");
        }
        if (employee.getSalary() < 0) {
            throw new InvalidEmployeeException("salary must not be negative, got " + employee.getSalary());
        }
    }

    @Override
    public void afterPropertiesSet() {
        System.out.println("[LIFECYCLE] EmployeeValidator initialised (InitializingBean)");
    }

    @Override
    public void destroy() {
        System.out.println("[LIFECYCLE] EmployeeValidator destroyed (DisposableBean)");
    }
}
