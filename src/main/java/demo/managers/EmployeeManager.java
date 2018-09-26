package demo.managers;

import demo.models.Employee;
import demo.models.exceptions.EmployeeNotFoundException;
import demo.repositories.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class EmployeeManager {
    @Autowired
    private EmployeeRepository employeeRepository;

    public List<Employee> getAll() {
        return employeeRepository.findAll();

    }

    public Employee create(Employee employee) {
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
                    return employeeRepository.save(foundEmployee);
                })
                .orElseGet(() -> {
                    // employee.setId(id); This is a bug, it will not allow overriding of generated ID without a
                    // customer sequence generator!
                    return employeeRepository.save(employee);
                });
    }

    public void removeEmployee(Long id) {
        employeeRepository.deleteById(id);
    }
}
