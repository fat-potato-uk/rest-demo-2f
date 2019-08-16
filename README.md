### Challenge 5

So, we are going to try and rectify some of the issues identified in the previous challenge now!

To start, we are going to make the transition to `JUnit5`. We could technically achieve this without
the move but it seems like a good a times as any to make the transition!

First, lets get rid of the `JUnit4` dependencies so we don't get tripped up by intelliJ being 
helpful and importing the wrong libraries for us! (_Side note, this is something of a classic gotcha, 
whereby the wrong annotations with the same name are ported in, presenting vague unhelpful errors
to the poor unsuspecting developer_)


We can do this by updating the `pom.xml` as follows:

```
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
    <exclusions>
        <exclusion>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
        </exclusion>
    </exclusions>
</dependency>
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter-api</artifactId>
    <version>5.3.2</version>
    <scope>test</scope>
</dependency>
```
This negates the `JUnit4` dependencies in the `spring-boot-starter-test` (avoiding accidental 
errors as described) and adds in the newer `JUnit`.

You'll need to make some changes now to get your project to work as before. There has been a change
in some of the annotations, so now your tests will need to look like this:

```
@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class EmployeeControllerTest { ... }
```
and
```
@ExtendWith(SpringExtension.class)
@SpringBootTest
class EmployeeRepositoryTest { ... }
```

You will also need to fix some of the imports too (intelliJ should help you with this).

Now we can go about fixing the controller tests!

First, our core mistake was wiring in the `EmployeeManager` rather than mocking it out. This extends
the scope of our tests and makes it much harder to control the behaviours we are looking to test.

It is for this reason why many people reserve this mechanic of testing for their "integration testing".
In fact, the `mockMvc` mechanic is technically part of the Spring _integration_ testing framework, for
this very reason.

I think we can still use it to a lesser extend to get some unit coverage however, and mocking will help
with this!

So, lets change a few things!

```
@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class EmployeeControllerTest {

    @MockBean
    private EmployeeRepository employeeRepository;
    
    @MockBean
    private EmployeeManager employeeManager;

    @Autowired
    private MockMvc mockMvc;
    
    ...
``` 

We have wired in a mock `EmployeeManager`. This will be injected at any point in which we `@Autowire`
an `EmployeeManager`. The same goes for the `EmployeeRepository`, but this is a side effect of our
contrived design mechanic of initialising the database via a bean on startup (otherwise it would
not be necessary).

Now we need to change our tests to behave under these mocks. Lets try the first one.

```
@Test
void getAllEmployeesTest() throws Exception {
    when(employeeManager.getAll()).thenReturn(List.of(new Employee("Bilbo Baggins", "burglar"), 
                                                      new Employee("Frodo Baggins", "thief")));
    
    this.mockMvc.perform(get("/employees"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Bilbo Baggins"))
            .andExpect(jsonPath("$[0].role").value("burglar"))
            .andExpect(jsonPath("$[1].name").value("Frodo Baggins"))
            .andExpect(jsonPath("$[1].role").value("thief"))
            .andDo(print()); // Handy for those of us who are rubbish at working out what Json should look like.
}
```
_Note: You may have noticed we can now change the scoping of our tests to `package-private`. `JUnit5`
does not have the same `public` scoping requirements to `JUnit4`, which is nice._ 

It could be argued that here we are simply testing Spring MVC behaviour (because we are), but typically
I find these tests useful for regression purposes should any change be made to your APIs. These 
verification actions can give you early insight into whether connecting components will experience
issues.

For tests like our delete test, we can use a `verify` to check the correct operation is performed:

```
@Test
void deleteEmployee() throws Exception {
    this.mockMvc.perform(delete("/employees/123")).andExpect(status().isOk());
    verify(employeeManager, times(1)).removeEmployee(123L);
}
```

As an exercise for the reader, try updating the other tests!

_Hint: You may need to update the `Employee` entity with the following:_

```
@EqualsAndHashCode(exclude = "id")
```

### Challenge 5b

Now we have re-written our tests, you may have noticed we have tanked our coverage in `EmployeeManager`:

![Coverage](coverage.png?raw=true "Coverage ")

Looks like something we best rectify!

There are arguments here for either using an in-memory DB test. For now however, we are going to mock
and database interactions.

For this, we are going to use the `MockitoExtension` and avoid any Spring context wiring:

```
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-junit-jupiter</artifactId>
    <version>2.23.0</version>
    <scope>test</scope>
</dependency>
```

With this added, we can use the following to get us started:

```
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
```

You may notice running this test how much faster it is to load. That's because we are not setting
up the Spring context for every test (class) run. The tests themselves take approximately the same
time (which is what intelliJ reports on), but it is observable the speed difference between the two.

 