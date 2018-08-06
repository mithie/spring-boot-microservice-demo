package my.demo.springboot.microservice.todo;

import my.demo.springboot.microservice.todo.api.TodoController;
import my.demo.springboot.microservice.todo.client.AccountClient;
import my.demo.springboot.microservice.todo.domain.Todo;
import my.demo.springboot.microservice.todo.domain.TodoService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.hamcrest.core.Is.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

@RunWith(SpringRunner.class)
@WebMvcTest(TodoController.class)
public class TodoServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TodoService todoService;

    private JacksonTester<Todo> insertTodo;

    private List<Todo> todos = new ArrayList<>();

    private final UUID accountOneId = UUID.randomUUID();
    private final UUID accountTwoId = UUID.randomUUID();

    @Before
    public void setup() {

        JacksonTester.initFields(this, new ObjectMapper());

        final Stream<String>
                todoStream = Stream.of(accountOneId.toString() + ",John.Doe@foo.bar,Clean Dishes,false", accountTwoId + ",John.Doe@foo.bar,Pay Bills,false");

        todos = todoStream.map(todo -> {
            String[] info = todo.split(",");
            return new Todo(UUID.randomUUID(), UUID.fromString(info[0]), info[1], info[2], Boolean.getBoolean(info[3]));
        }).collect(Collectors.toCollection(ArrayList::new));

    }

    @Test
    public void testGetTodoSuccess() throws Exception {

        given(todoService.findAllById(accountOneId)).willReturn(todos);

        final ResultActions result = mockMvc.perform(get("/accounts/" +accountOneId +"/todos"));
        result.andExpect(status().is2xxSuccessful());
    }

    @Test
    public void testGetTodoResponseEqualsAccountId2() throws Exception {

        given(todoService.findAllById(accountOneId)).willReturn(todos);

        final ResultActions result = mockMvc.perform(get("/accounts/"+accountOneId +"/todos"));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.todoResourceList[0].todo.todoId", is(todos.get(0).getTodoId().toString())))
                .andExpect(jsonPath("_embedded.todoResourceList[0].todo.accountId", is(todos.get(0).getAccountId().toString())))
                .andExpect(jsonPath("_embedded.todoResourceList[0].todo.email", is(todos.get(0).getEmail())))
                .andExpect(jsonPath("_embedded.todoResourceList[0].todo.description", is(todos.get(0).getDescription())))
                .andExpect(jsonPath("_embedded.todoResourceList[0].todo.completed", is(todos.get(0).isCompleted())))
                .andExpect((jsonPath("_embedded.todoResourceList[0]._links.self.href", containsString("/accounts/" + accountOneId + "/todos"))));
    }

    @Test
    public void testPostTodoRequestForExistingAccount() throws Exception {

        Todo newTodo = new Todo(UUID.randomUUID(), accountOneId,"John.Doe@foo.bar","Smoke Cigar",true);
        todos.add(newTodo);

        given(todoService.addTodo(newTodo)).willReturn(todos.get(2));

        final ResultActions result = mockMvc.perform(post("/todos").contentType(MediaType.APPLICATION_JSON)
                .content(insertTodo.write(newTodo).getJson()));

        result.andExpect(status().isCreated())
                .andExpect(jsonPath("todo.todoId", is(todos.get(2).getTodoId().toString())))
                .andExpect(jsonPath("todo.accountId", is(todos.get(2).getAccountId().toString())))
                .andExpect(jsonPath("todo.email", is(todos.get(2).getEmail())))
                .andExpect(jsonPath("todo.description", is(todos.get(2).getDescription())))
                .andExpect(jsonPath("todo.completed", is(todos.get(2).isCompleted())))
                .andExpect((jsonPath("_links.self.href", containsString("/accounts/" + accountOneId +"/todos"))));

        given(todoService.findAllById(accountOneId)).willReturn(todos);

        final ResultActions requestResults = mockMvc.perform(get("/accounts/" + accountOneId + "/todos"));

        requestResults .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.todoResourceList", hasSize(3)));
    }
}