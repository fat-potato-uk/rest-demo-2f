package demo.managers;

import demo.models.Employee;
import demo.repositories.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeManagerTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeManager employeeManager;

    // Some test employees we are going to use over and over again
    private final Employee bob = new Employee("Bob", "Builder");
    private final Employee sam = new Employee("Sam", "Arsonist");

    @Test
    void getAllTest() {
        var employees = List.of(bob, sam);
        when(employeeManager.getAll()).thenReturn(employees);
        assertThat(employeeManager.getAll(), contains(bob, sam));
    }

    @Test
    void createTest() {
    }

    @Test
    void getEmployeeTest() {
    }

    @Test
    void replaceOrCreateEmployeeTest() {
    }

    @Test
    void removeEmployeeTest() {
    }
}