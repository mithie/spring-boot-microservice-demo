package my.demo.springboot.microservice.todo.it;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.client.Traverson;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import my.demo.springboot.microservice.todo.domain.Todo;
import my.demo.springboot.microservice.todo.domain.TodoResources;

public class TodoApplicationIntegrationTest {

    RestTemplate restTemplate = RestTemplateBuilder.restTemplate();

    private final String VALID_TODO_URL="/accounts/4e696b86-257f-4887-8bae-027d8e883638/todos";
    private final String INVALID_TODO_URL="/accounts/4e696b86-257f-4887-8bae-027d8e883637/todos";

    private final Todo expectedOne = new Todo(UUID.fromString("4e696b86-257f-4887-8bae-027d8e883638"), "John.Doe@foo.bar", "Clean Dishes", false);
    private final Todo expectedTwo = new Todo(UUID.fromString("4e696b86-257f-4887-8bae-027d8e883638"), "John.Doe@foo.bar", "Watch NBA", false);


    @BeforeClass
    public static void setup() {
        ServiceEnvironment.startServices();
    }

    @AfterClass
    public static void tearDown() {
        ServiceEnvironment.shutdownServices();
    }

    @Test
    public void givenTodos_whenFindAllByAccount_thenEqualsSampleValues() throws Exception {
        startIfDown(Instance.ACCOUNT);
        assertNotNull(ServiceEnvironment.getInstance(Instance.ACCOUNT));

        final ArrayList<Todo> todos = new ArrayList<>(traverseTodos());

        assertEquals(expectedOne, todos.get(0));
        assertEquals(expectedTwo, todos.get(1));
    }

    @Test
    public void givenTodos_whenFindAllByWrongAccount_thenAccountNotFound() {
        startIfDown(Instance.ACCOUNT);
        assertNotNull(ServiceEnvironment.getInstance(Instance.ACCOUNT));

        assertTrue(errorResponseFromTodoService().contains("Account with id 4e696b86-257f-4887-8bae-027d8e883637 not found"));
    }

    @Test
    public void givenTodos_whenFindAllByAccountAndAccountServiceIsDown_thenEqualsSampleValuesFromFallback() throws Exception {
        stopIfRunning(Instance.ACCOUNT, ServiceEnvironment.TODO_SERVICE_PORT);
        assertNull(ServiceEnvironment.getInstance(Instance.ACCOUNT));

        final ArrayList<Todo> todos = new ArrayList<>(traverseTodos());

        assertEquals(expectedOne, todos.get(0));
        assertEquals(expectedTwo, todos.get(1));
    }

    @Test
    public void givenTodos_whenFindAllByWrongAccountAndAccountServiceIsDown_thenAccountNotFoundInCache() throws Exception {
        stopIfRunning(Instance.ACCOUNT, ServiceEnvironment.TODO_SERVICE_PORT);
        assertNull(ServiceEnvironment.getInstance(Instance.ACCOUNT));

        assertTrue(errorResponseFromTodoService().contains("Account with id 4e696b86-257f-4887-8bae-027d8e883637 not found in cache"));
    }

    @Test
    public void givenTodos_whenPostNewTodo_thenResponseEqualsTodo() throws Exception {
        startIfDown(Instance.ACCOUNT);
        assertNotNull(ServiceEnvironment.getInstance(Instance.ACCOUNT));

        final Todo newTodo = new Todo(UUID.fromString("4e696b86-257f-4887-8bae-027d8e883638"), "John.Doe@foo.bar", "Book Flight to NY", true);
        final HttpEntity<Todo> request = new HttpEntity<>(newTodo);
        final ResponseEntity<Todo> response = new RestTemplate()
                .postForEntity(withUrl(ServiceEnvironment.TODO_SERVICE_PORT, "/todos"), request, Todo.class);

        assertEquals(newTodo, response.getBody());
    }

    private void startIfDown(final Instance instance) {
        final SpringBootServiceWrapper service = ServiceEnvironment.getInstance(instance);
        if(service==null) {
            ServiceEnvironment.addAccountInstance().startService();
        }
    }

    private void stopIfRunning(final Instance instance, final int port) {
        final SpringBootServiceWrapper service = ServiceEnvironment.getInstance(instance);
        if(service!=null) {
            service.stopService();

            await().atMost(SpringBootServiceWrapper.MAX_ENDPOINT_SHUTDOWN_TIME, TimeUnit.SECONDS).until(service.endpointIsDown());
            await().atMost(SpringBootServiceWrapper.MAX_ENDPOINT_DEREG_TIME, TimeUnit.SECONDS)
                    .until(service.endpointIsDeregistered(withUrl(port, VALID_TODO_URL)));

            ServiceEnvironment.removeInstance(instance);
        }
    }

    private Collection<Todo> traverseTodos() throws Exception {
        final Traverson traverson = new Traverson(new URI(
                withUrl(ServiceEnvironment.TODO_SERVICE_PORT, VALID_TODO_URL)), MediaTypes.HAL_JSON);

        final ResponseEntity<TodoResources> todoResources = traverson.
                follow("$._embedded.todos[0]._links.accountTodos.href").
                toEntity(TodoResources.class);

        return todoResources.getBody().getContent();
    }

    private String errorResponseFromTodoService() {
        final String url = withUrl(ServiceEnvironment.TODO_SERVICE_PORT, INVALID_TODO_URL);

        String responseBody = null;

        try {
            restTemplate.getForEntity(url, JSONObject.class);
        } catch(final HttpClientErrorException e) {
            responseBody = e.getResponseBodyAsString();
        }
        return responseBody;
    }

    private String withUrl(final int port, final String uri) {
        return "http://localhost:" + port + uri;
    }
}