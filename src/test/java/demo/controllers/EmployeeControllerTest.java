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

    @Test
    public void getOneEmployeeTest() throws Exception {
        this.mockMvc.perform(get("/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Bilbo Baggins"))
                .andExpect(jsonPath("$.role").value("burglar"));
    }

    @Test
    public void getOneEmployeeNotFoundTest() throws Exception {
        this.mockMvc.perform(get("/employees/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("No Employee found with ID: 99"));
    }

    @Test
    public void replaceOrCreateEmployee() throws Exception {
        this.mockMvc.perform(put("/employees/10")
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .content("{\"name\":\"Harry Potter\",\"role\":\"Rubbish Wizard\"}"))
                .andExpect(status().isOk());
    }

    @Test
    public void deleteEmployee() throws Exception {
        final var employee = new Employee("Bob", "Builder");
        employeeRepository.saveAndFlush(employee);
        this.mockMvc.perform(delete(format("/employees/%d", employee.getId())))
                .andExpect(status().isOk());
    }
}
