package my.demo.springboot.microservice.todo.api;

import my.demo.springboot.microservice.todo.client.AccountClient;
import my.demo.springboot.microservice.todo.domain.Todo;
import my.demo.springboot.microservice.todo.persistence.TodoRepository;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.rest.webmvc.PersistentEntityResource;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.config.PersistentEntityResourceAssemblerArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@RunWith(SpringJUnit4ClassRunner.class)
//@WebAppConfiguration
//@RunWith(SpringRunner.class)
//@WebMvcTest(TodoController.class)
//@SpringBootTest
//@AutoConfigureMockMvc
//@ContextConfiguration(classes = {TodoApplicationConfiguration.class, WebConfiguration.class})
//@EnableSpringDataWebSupport
public class TodoControllerTest {

    @Mock
    PersistentEntityResourceAssembler assembler;

    @Mock
    PersistentEntityResourceAssemblerArgumentResolver assemblerResolver;

    @Mock
    PersistentEntity<List<Todo>, ?> entity;

    @Mock
    AccountClient accountClient;

    @Mock
    TodoRepository todoRepository;

    @InjectMocks
    TodoController todoController;

    private MockMvc mockMvc;

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    /*
    @Autowired
    private MockMvc mockMvc;

    //@Autowired
    //private WebApplicationContext webApplicationContext;

    @MockBean
    AccountClient accountClient;

    @MockBean
    TodoRepository todoRepository;

    //@MockBean
    //private TodoServiceImpl todoService;

    //private JacksonTester<Todo> insertTodo;
    */

    private final List<Todo> todos = new ArrayList<>();

    private final UUID accountOneId = UUID.fromString("4e696b86-257f-4887-8bae-027d8e883638");
    private final UUID accountTwoId = UUID.randomUUID();

    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(todoController)
                .setCustomArgumentResolvers(assemblerResolver)
                .build();

        //mockMvc = webAppContextSetup(webApplicationContext).build();

        //JacksonTester.initFields(this, new ObjectMapper());

        todos.add(new Todo(UUID.randomUUID(), accountOneId, "John.Doe@foo.bar", "Clean Dishes", false));
        //todos.add(new Todo(UUID.randomUUID(), accountTwoId, "John.Doe@foo.bar", "Pay Bills", false));
    }

    @Test
    public void testGetTodoSuccess() throws Exception {
        //given(todoService.findAllByAccount(accountOneId)).willReturn(todos);
        when(accountClient.isAccountValid(accountOneId)).thenReturn(true);
        when(todoRepository.findByAccountId(accountOneId)).thenReturn(todos);
        //given(todoRepository.findByAccountId(accountOneId)).willReturn(todos);

        when(assemblerResolver.supportsParameter(any())).thenReturn(true);
        when(assemblerResolver.resolveArgument(any(), any(), any(), any())).thenReturn(assembler);
        when(assembler.toResource(todos)).thenReturn(PersistentEntityResource.build(todos, entity).build());

        final ResultActions result = mockMvc.perform(get("/todos/search/accounts")
                                            .param("accountId", accountOneId.toString())
                                            .contentType("application/hal+json"))
                                            .andDo(print());
        //final ResultActions result = mockMvc.perform(get("/todos").contentType("application/hal+json")).andDo(print());

        result.andExpect(status().is2xxSuccessful());
        result.andExpect(jsonPath("_embedded.todos", hasSize(1)));
    }

    /*
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
    */
}