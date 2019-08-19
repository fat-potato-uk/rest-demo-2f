package demo.managers;

import demo.models.Employee;
import demo.models.exceptions.EmployeeNotFoundException;
import demo.repositories.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EmployeeManagerTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Captor
    private ArgumentCaptor<Employee> employeeCaptor;

    @Spy
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
        // This will always return whatever we try to save as the repository does
        when(employeeRepository.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        // Skip the expensive call
        doNothing().when(employeeManager).calculateSalary(employeeCaptor.capture());

        assertEquals(bob, employeeManager.create(bob));
        verify(employeeRepository, times(1)).save(eq(bob));
        assertEquals(bob, employeeCaptor.getValue());
    }

    @Test
    void getEmployeeTest() throws EmployeeNotFoundException {
        when(employeeRepository.findById(eq(1L))).thenReturn(Optional.of(bob));
        assertEquals(bob, employeeManager.getEmployee(1L));
    }

    @Test
    void getEmployeeErrorTest() {
        when(employeeRepository.findById(eq(1L))).thenReturn(Optional.empty());
        var thrown = assertThrows(EmployeeNotFoundException.class, () -> employeeManager.getEmployee(1L));
        assertEquals("No Employee found with ID: 1", thrown.getMessage());
    }

    @Test
    void replaceOrCreateEmployeeNewTest() {
        // This will always return whatever we try to save as the repository does
        when(employeeRepository.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        // Skip the expensive call
        doNothing().when(employeeManager).calculateSalary(employeeCaptor.capture());

        when(employeeRepository.findById(eq(1L))).thenReturn(Optional.of(bob));
        assertEquals(sam, employeeManager.replaceOrCreateEmployee(1L, sam));

        verify(employeeRepository, times(1)).save(eq(sam));
        assertEquals(sam, employeeCaptor.getValue());
    }

    @Test
    void replaceOrCreateEmployeeExistingTest() {
        // This will always return whatever we try to save as the repository does
        when(employeeRepository.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        // Skip the expensive call
        doNothing().when(employeeManager).calculateSalary(employeeCaptor.capture());

        when(employeeRepository.findById(eq(1L))).thenReturn(Optional.empty());
        assertEquals(bob, employeeManager.replaceOrCreateEmployee(1L, bob));

        // The equality operator does not take into account Ids
        verify(employeeRepository, times(1)).save(eq(bob));
        assertEquals(bob, employeeCaptor.getValue());
    }

    @Test
    void removeEmployeeTest() {
        employeeManager.removeEmployee(1L);
        verify(employeeRepository, times(1)).deleteById(eq(1L));
    }
}