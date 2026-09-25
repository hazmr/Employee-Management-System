package org.haz.repository;

import org.haz.model.Employee;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@Profile("prod")
public class FileBackedEmployeeRepository implements EmployeeRepository {
    @Value("${storage.file-path}")
    private String filePath;

    private final List<Employee> cache = new ArrayList<>();

    @PostConstruct
    public void loadFromDisk() {
        Path path = Path.of(filePath);
        if (Files.exists(path)) {
            try {
                for (String line : Files.readAllLines(path)) {
                    if (!line.isBlank()) {
                        cache.add(parse(line));
                    }
                }
            } catch (IOException e) {
                System.out.println("[WARN] could not read " + filePath + ": " + e.getMessage());
            }
        }
        System.out.println("[LIFECYCLE] FileBackedEmployeeRepository initialised (prod profile), "
                + cache.size() + " record(s) loaded from " + filePath);
    }

    @Override
    public void save(Employee employee) {
        findById(employee.getId()).ifPresent(cache::remove);
        cache.add(employee);
        flush();
    }

    @Override
    public Optional<Employee> findById(int id) {
        return cache.stream().filter(e -> e.getId() == id).findFirst();
    }

    @Override
    public List<Employee> findAll() {
        return new ArrayList<>(cache);
    }

    @PreDestroy
    public void flushOnShutdown() {
        flush();
        System.out.println("[LIFECYCLE] FileBackedEmployeeRepository destroyed, data written to " + filePath);
    }

    private void flush() {
        List<String> lines = cache.stream().map(this::format).toList();
        try {
            Files.write(Path.of(filePath), lines);
        } catch (IOException e) {
            System.out.println("[WARN] could not write " + filePath + ": " + e.getMessage());
        }
    }

    private String format(Employee employee) {
        return employee.getId() + "," + employee.getName() + ","
                + employee.getDepartment() + "," + employee.getSalary();
    }

    private Employee parse(String line) {
        String[] parts = line.split(",");
        return new Employee(Integer.parseInt(parts[0].trim()), parts[1].trim(),
                parts[2].trim(), Double.parseDouble(parts[3].trim()));
    }
}
