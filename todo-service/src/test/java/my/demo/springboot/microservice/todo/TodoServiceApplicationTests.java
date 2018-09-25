package my.demo.springboot.microservice.todo;

import com.fasterxml.jackson.databind.ObjectMapper;
import my.demo.springboot.microservice.todo.domain.Todo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {TodoApplicationConfiguration.class, WebConfiguration.class})
public class TodoServiceApplicationTests {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private final List<Todo> todos = new ArrayList<>();

    private final UUID accountOneId = UUID.fromString("4e696b86-257f-4887-8bae-027d8e883638");
    private final UUID accountTwoId = UUID.randomUUID();

    private JacksonTester<Todo> insertTodo;

    @Before
    public void setup() {
        mockMvc = webAppContextSetup(webApplicationContext).build();

        JacksonTester.initFields(this, new ObjectMapper());

        todos.add(new Todo(UUID.randomUUID(), accountOneId, "John.Doe@foo.bar", "Clean Dishes", false));
        todos.add(new Todo(UUID.randomUUID(), accountTwoId, "John.Doe@foo.bar", "Pay Bills", false));
    }

    @Test
    public void testGetTodoSuccess() throws Exception {
        final ResultActions result = mockMvc.perform(get("/todos/search/accounts")
                                            .param("accountId", accountOneId.toString())
                                            .contentType("application/hal+json"))
                                            .andDo(print());

        result.andExpect(status().is2xxSuccessful());
        result.andExpect(jsonPath("_embedded.todos", hasSize(2)));
    }

    @Test
    public void testGetTodoError() throws Exception {
        final ResultActions result = mockMvc.perform(get("/todos/search/accounts")
                .param("accountId", accountTwoId.toString())
                .contentType("application/hal+json"))
                .andDo(print());

        result.andExpect(status().is2xxSuccessful());
        result.andExpect(jsonPath("_embedded.todos", hasSize(0)));
    }


    @Test
    public void testPostTodoRequestForExistingAccount() throws Exception {

        final Todo newTodo = new Todo(UUID.randomUUID(), accountOneId,"John.Doe@foo.bar","Smoke Cigar",true);
        todos.add(newTodo);

        ResultActions result = mockMvc.perform(post("/todos").contentType(MediaType.APPLICATION_JSON)
                .content(insertTodo.write(newTodo).getJson())).andDo(print());

        result.andExpect(status().isCreated());

        result = mockMvc.perform(get("/todos/search/accounts")
                .param("accountId", accountOneId.toString())
                .contentType("application/hal+json"))
                .andDo(print());

        result.andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.todos", hasSize(3)));
    }
}