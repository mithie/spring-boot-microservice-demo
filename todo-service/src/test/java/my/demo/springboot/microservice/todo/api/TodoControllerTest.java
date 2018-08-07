package my.demo.springboot.microservice.todo.api;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;

import my.demo.springboot.microservice.todo.domain.Todo;
import my.demo.springboot.microservice.todo.domain.TodoServiceImpl;

@RunWith(SpringRunner.class)
@WebMvcTest(TodoController.class)
public class TodoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TodoServiceImpl todoService;

    private JacksonTester<Todo> insertTodo;

    private final List<Todo> todos = new ArrayList<>();

    private final UUID accountOneId = UUID.randomUUID();
    private final UUID accountTwoId = UUID.randomUUID();

    @Before
    public void setup() {
        JacksonTester.initFields(this, new ObjectMapper());

        todos.add(new Todo(UUID.randomUUID(), accountOneId, "John.Doe@foo.bar", "Clean Dishes", false));
        todos.add(new Todo(UUID.randomUUID(), accountTwoId, "John.Doe@foo.bar", "Pay Bills", false));
    }

    @Test
    public void testGetTodoSuccess() throws Exception {
        given(todoService.findAllByAccount(accountOneId)).willReturn(todos);

        final ResultActions result = mockMvc.perform(get("/accounts/" + accountOneId +"/todos"));
        result.andExpect(status().is2xxSuccessful());
    }

    @Test
    public void testGetTodoResponseEqualsAccountId2() throws Exception {
        given(todoService.findAllByAccount(accountOneId)).willReturn(todos);

        final ResultActions result = mockMvc.perform(get("/accounts/" + accountOneId +"/todos"));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.todos[0].todoId", is(todos.get(0).getTodoId().toString())))
                .andExpect(jsonPath("_embedded.todos[0].accountId", is(todos.get(0).getAccountId().toString())))
                .andExpect(jsonPath("_embedded.todos[0].email", is(todos.get(0).getEmail())))
                .andExpect(jsonPath("_embedded.todos[0].description", is(todos.get(0).getDescription())))
                .andExpect(jsonPath("_embedded.todos[0].completed", is(todos.get(0).isCompleted())))
                .andExpect((jsonPath("_embedded.todos[0]._links.accountTodos.href", containsString("/accounts/" + accountOneId
                        + "/todos"))));
    }

    @Test
    public void testPostTodoRequestForExistingAccount() throws Exception {

        final Todo newTodo = new Todo(UUID.randomUUID(), accountOneId,"John.Doe@foo.bar","Smoke Cigar",true);
        todos.add(newTodo);

        given(todoService.addTodo(newTodo)).willReturn(todos.get(2));

        final ResultActions result = mockMvc.perform(post("/todos").contentType(MediaType.APPLICATION_JSON)
                .content(insertTodo.write(newTodo).getJson()));

        result.andExpect(status().isCreated())
                .andExpect(jsonPath("todoId", is(todos.get(2).getTodoId().toString())))
                .andExpect(jsonPath("accountId", is(todos.get(2).getAccountId().toString())))
                .andExpect(jsonPath("email", is(todos.get(2).getEmail())))
                .andExpect(jsonPath("description", is(todos.get(2).getDescription())))
                .andExpect(jsonPath("completed", is(todos.get(2).isCompleted())))
                .andExpect((jsonPath("_links.accountTodos.href", containsString("/accounts/" + accountOneId +"/todos"))));

        given(todoService.findAllByAccount(accountOneId)).willReturn(todos);

        final ResultActions requestResults = mockMvc.perform(get("/accounts/" + accountOneId + "/todos"));

        requestResults .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.todos", hasSize(3)));
    }
}