package demo.managers;

import demo.models.Employee;
import demo.models.exceptions.EmployeeNotFoundException;
import demo.repositories.EmployeeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
@Slf4j
public class EmployeeManager {
    @Autowired
    private EmployeeRepository employeeRepository;

    public List<Employee> getAll() {
        return employeeRepository.findAll();
    }

    public Employee create(Employee employee) {
        calculateSalary(employee);
        return employeeRepository.save(employee);
    }

    public Employee getEmployee(Long id) throws EmployeeNotFoundException {
        return employeeRepository.findById(id).orElseThrow(() -> new EmployeeNotFoundException(id));
    }

    public Employee replaceOrCreateEmployee(Long id, Employee employee) {
        return employeeRepository.findById(id)
                .map(foundEmployee -> {
                    foundEmployee.setName(employee.getName());
                    foundEmployee.setRole(employee.getRole());
                    calculateSalary(employee);
                    return employeeRepository.save(foundEmployee);
                })
                .orElseGet(() -> {
                    calculateSalary(employee);
                    return employeeRepository.save(employee);
                });
    }

    public void removeEmployee(Long id) {
        employeeRepository.deleteById(id);
    }

    void calculateSalary(Employee employee) {
        try {
            // Do very expensive calculation
            Thread.sleep(1000);
            // Then just make up a number
            employee.setSalary((new Random()).nextLong());
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }
    }
}
