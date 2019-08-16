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

