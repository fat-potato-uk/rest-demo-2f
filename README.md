### Challenge 6

For this example we are going to be paying our employees!

```
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
```

Like all good employers we take our time in offering the right compensation for our staff. For our 
tests to continue working, we will need to exclude the salary from the comparison function generated
by `Lombok`:

```
@Data
@Entity
@NoArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode(exclude = {"id", "salary"})
public class Employee {
    @Id
    @GeneratedValue
    private Long id;
    @NonNull
    private String name;
    @NonNull
    private String role;
    private Long salary;
}
```

Update all the functions that `save` an `Employee` to the database so that we always set a salary.

The problem now, as you may have noticed, is that this has had a rather negative effect on our tests:

![Runtime](testTime.png?raw=true "Runtime")

For the purposes of our contrived example, we don't really care about the salary calculation function
given its basic nature. Both the functions are well tested external libraries and not within our remit
to test, so lets skip that call!

_Note: As you may have noticed, the function was declared as package private. This is a classic work 
around to testing with private calls. It's not ideal, but you will see why its necessary shortly._

As we are testing against a concrete object, we cannot simply "mock" the call in the same way, we can
however "spy" on it:

```
@Spy
@InjectMocks
private EmployeeManager employeeManager;
```

This allows us to mock out calls in the `EmployeeManager` now as if it were a mock. For example:

```
@Test
void createTest() {
    // This will always return whatever we try to save as the repository does
    when(employeeRepository.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

    // Skip the expensive call
    doNothing().when(employeeManager).calculateSalary(any());
    
    assertEquals(bob, employeeManager.create(bob));
    verify(employeeRepository, times(1)).save(eq(bob));
}
```

Make the same changes to the other functions where required.

Now our run times should be back to normal! Spies are very handy for getting good coverage in unit
tests by exercising paths that may otherwise be complicated or involved to do. You can also wire
them in via the `@SpyBean` annotation (akin to the `@MockBean`)

We do have a slight issue though. Ideally, we would like to confirm the _right_ employees are 
being passed into the call. We can do this through an argument captor:

```
@Captor
private ArgumentCaptor<Employee> employeeCaptor;
```

To use the captor, we can pass it into the `Spy` mocking call:

```
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
```

Now we are avoiding the costly call but also checking our code behaves in the way we expect!

Update the other tests accordingly.