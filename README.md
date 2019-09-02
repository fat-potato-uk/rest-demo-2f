### Challenge 7

As the final polish to our application, we are going to add metrics. For this contrived example, we
are going to add some metrics to the `EmployeeManager` to increment counts for each method called.

Handily, Spring Boot has good support metrics. Via the `Actuator` module, it is trivial to add 
metrics:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

Adding these to the `pom.xml` allows us to use the `MeterRegistry` bean as well as adding Prometheus
support to the application (without directly coupling us to a metrics solution).

Update the `EmployeeManager` so it now resembles this:

```java
package demo.managers;

import demo.models.Employee;
import demo.models.exceptions.EmployeeNotFoundException;
import demo.repositories.EmployeeRepository;
import io.micrometer.core.instrument.MeterRegistry;
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

    @Autowired
    private MeterRegistry meterRegistry;

    public List<Employee> getAll() {
        meterRegistry.counter("employee_manager", "action", "get").increment();
        return employeeRepository.findAll();
    }

    public Employee create(Employee employee) {
        meterRegistry.counter("employee_manager", "action", "create").increment();
        calculateSalary(employee);
        return employeeRepository.save(employee);
    }

    public Employee getEmployee(Long id) throws EmployeeNotFoundException {
        meterRegistry.counter("employee_manager", "action", "get").increment();
        return employeeRepository.findById(id).orElseThrow(() -> new EmployeeNotFoundException(id));
    }

    public Employee replaceOrCreateEmployee(Long id, Employee employee) {
        meterRegistry.counter("employee_manager", "action", "create").increment();
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
        meterRegistry.counter("employee_manager", "action", "remove").increment();
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
```

It's worth noting that there is a lot of different approach you can take with creating metrics. This
shows one approach whereby we rely on `MeterRegistry` behaviour not to recreate metrics with the
same name and tags, but to increment previously created entries. We could have easily done something
like this:

```java
private Counter removeCounter;

public EmployeeManager(@Autowired MeterRegistry meterRegistry) {
    removeCounter = meterRegistry.counter("employee_manager", "action", "remove");
    ...
}
       
...

public void removeEmployee(Long id) {
    removeCounter.increment();
    employeeRepository.deleteById(id);
}
```

Which would achieve the same outcome. Much of how metrics are created depends on the manner to which
they used, or more importantly, how they are to be tested (more on this to follow!).

A final step is required before we can test our metrics, a configuration file is required:

```
├── pom.xml
├── rest-demo-2.iml
├── src
│   ├── main
│   │   │ 
│   │   └── resources
│   │       └── application.yaml

```

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus
```

The `application.yaml` configures `Actuator` so the appropriate endpoints are opened up.

If you run the application (`spring-boot:run`) and navigate to http://localhost:8080/actuator/prometheus
you can see all of the metrics our application is providing.

If you run one of the `curl` commands from before, you can check that the metrics are incremented
accordingly:

```
employee_manager_total{action="get",} 4.0
```

You may also note that none of the other metrics are available. That's because we have created them
in a dynamic manner. Until we hit those endpoints we will not create the corresponding counters.

As an exercise, look to correct this behaviour and test the counter in the unit tests.

_Note: Avoid using "real" `MeterRegistry` implementation wherever possible. This can lead to
scenarios where you need to run `@DirtiesContext` in order to reset counters which is very costly!_

##### Hints

If the struggle is too much :) 

* As suggested, creating the metrics on construction of a bean helps avoid the
 "dynamic metric" effect:
 
 ```java
...
    private final Counter createCounter;
    private final Counter getCounter;
    private final Counter removeCounter;

    public EmployeeManager(@Autowired MeterRegistry meterRegistry) {
        createCounter = meterRegistry.counter("employee_manager", "action", "create");
        getCounter    = meterRegistry.counter("employee_manager", "action", "get");
        removeCounter = meterRegistry.counter("employee_manager", "action", "remove");
    }
...
```
* Each method only increments a single mock. This can make testing easier as we can re-use
the same mock `Counter` each time.

* `JUnit 5` provides a few ways to setup and clear down tests. These can be used in setup
of the mocks required:

```java
...
    @Mock
    private EmployeeRepository employeeRepository;

    @Captor
    private ArgumentCaptor<Employee> employeeCaptor;

    private EmployeeManager employeeManager;

    private Counter mockCounter;

    @BeforeEach // This happens before each test and after field initialisation
    void beforeEach() {
        setField(employeeManager, "employeeRepository", employeeRepository);
        reset(mockCounter);
    }

    @BeforeAll // This happens before Mockito initialises all the fields
    void beforeAll() {
        mockCounter = mock(Counter.class);
        var meterRegistry = mock(MeterRegistry.class);

        // We can reuse the same mock in our use case
        when(meterRegistry.counter(anyString(), ArgumentMatchers.<String>any())).thenReturn(mockCounter);

        employeeManager = spy(new EmployeeManager(meterRegistry));
    }
...  
```

