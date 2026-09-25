package org.haz.service;

import org.haz.model.Employee;

import java.util.List;
import java.util.Optional;

public interface EmployeeService {
    void addEmployee(Employee employee);

    Optional<Employee> getEmployeeById(int id);

    List<Employee> getAllEmployees();

    double giveRaise(int id, double percentage);

    String getCompanyName();

    String getCurrency();

    double getMaxRaisePercentage();
}
