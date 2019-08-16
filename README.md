### Challenge 4

So for this challenge we are going to test our service we just created. We are going to commit a couple
of _faux pas_ in doing so, but we will work to correct these in the subsequent tutorials!

Lets start with a couple of tests for our `GET` and `POST` endpoints:

```
package demo.controllers;


import demo.models.Employee;
import demo.repositories.EmployeeRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static java.lang.String.format;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class EmployeeControllerTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void getAllEmployeesTest() throws Exception {
        this.mockMvc.perform(get("/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Bilbo Baggins"))
                .andExpect(jsonPath("$[0].role").value("burglar"))
                .andExpect(jsonPath("$[1].name").value("Frodo Baggins"))
                .andExpect(jsonPath("$[1].role").value("thief"))
                .andDo(print()); // Handy for those of us who are rubbish at working out what Json should look like.
    }

    @Test
    public void createNewEmployeeTest() throws Exception {
        this.mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .content("{\"name\":\"Harry Potter\",\"role\":\"Rubbish Wizard\"}"))
                .andExpect(status().isOk());
    }
```

These tests verify that the initial entries loaded in the database are returned and when we add a new entry
that we get the correct response.

The `jsonPath` approach is not the only mechanic we can use in testing the return result, but it serves well
to highlight on how we can pull out fields of interest very easily with the framework.

###### Beware, here be dragons!
_Now, as mentioned before, we are committing a few subtle faux pas in our approach. By virtue of the ordering_
_and use of `List` collections, should the second test be run first (as Junit guarantees no order of execution)_
_we don't cause the first test to break. This may also be described as dumb luck. We will look in the next_
_challenge to correct this!_

##### And now for the rest

For this challenge, look to write tests for all the other endpoints. You can check your coverage in intelliJ
by using the little shield icon in the run bar:

![Icon Location](IconLocation.png?raw=true "Coverage Button")

