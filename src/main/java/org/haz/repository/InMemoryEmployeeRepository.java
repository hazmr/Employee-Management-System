package org.haz.repository;

import org.haz.model.Employee;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@Profile("dev")
public class InMemoryEmployeeRepository implements EmployeeRepository {
    private final Map<Integer, Employee> store = new LinkedHashMap<>();

    @PostConstruct
    public void init() {
        System.out.println("[LIFECYCLE] InMemoryEmployeeRepository initialised (dev profile)");
    }

    @Override
    public void save(Employee employee) {
        store.put(employee.getId(), employee);
    }

    @Override
    public Optional<Employee> findById(int id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Employee> findAll() {
        return new ArrayList<>(store.values());
    }

    @PreDestroy
    public void cleanUp() {
        System.out.println("[LIFECYCLE] InMemoryEmployeeRepository destroyed, dropping "
                + store.size() + " record(s)");
    }
}
