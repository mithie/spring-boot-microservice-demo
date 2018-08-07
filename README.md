# 07_Integration_Testing

## Demonstrated Principle

**07_Integration_Testing** is going to investigate how to write integration tests besides of the unit tests
that are already in place. In end-to-end testing scenarios it quickly becomes very difficult to have the complete application
stack running on a separate and a fully operated environment. It might soon become a very time consuming task to have a
complete and production like setup running in parallel. Moreover there are usually external services which are not even under the
control of our team. This makes integration testing even more difficult and error prone. Also, if you think about testing correct service
discovery- and load-balancing behavior this is going to be almost impossible in standard unit testing scenarios and not even what you'd want to
do in a unit test.

### Why integration testing is vital?

Writing unit tests is essential for every software project. Nevertheless unit tests are only one half of the truth since some
errors might only happen at runtime, which cannot be tested within a mocked or unit tested environment.

Let's see what's meant by this. Just have a look at the following code. A new Todo is being created and - if the check against
`account-service` is valid - it will be added to the repository. But then the initially passed `todo` is returned instead of `created`.
In consequence the returned Todo object won't contain a todo id, but a null value.

`spring-boot-microservice-demo/todo-service/src/main/java/my/demo/springboot/microservice/todo/domain/TodoServiceImpl.java`
```java
public Todo addTodo(final Todo todo) {
    Todo created = new Todo(UUID.randomUUID(), todo.getAccountId(), todo.getEmail(), todo.getDescription(), todo.isCompleted());

    List<Todo> todos = findAllByAccount(created.getAccountId());
    if(todos==null) {
        todos = new ArrayList<>();
    }

    if(todos.stream().filter(t -> t.equals(created)).count()==1) {
        throw new IllegalArgumentException("Todo " + created + " already exists");
    }

    todoConfiguration.todoRepository().get(created.getAccountId()).add(created);

    return todo;
}
```

Of course we already have an existing unit test for this use case. Let's look at this, too. In this test a new Todo will be created manually
and `addTodo` from class `TodoServiceImpl` will be mocked. We explicitly tell our mock that it should return the newly created todo when it is
added through the 'addTodo' method, because that is what we expect the method to do. In our unit test this is exactly what we wanted to test and it perfectly mimics the expected behavior
of `TodoServiceImpl`. But from the findings above we know that the returned value is something different and at runtime there will be a response with missing todo id.

And this is where the pain begins! Even though we did everything right in our unit test code and got a positive result from the test, we were not able to test the runtime behavior of
`TodoServiceImpl` which would return a different and incomplete result which we certainly wouldn't expect.

`spring-boot-microservice-demo/todo-service/src/test/java/my/demo/springboot/microservice/todo/api/TodoControllerTest.java`
```
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
```

There are many more possibilities why we might be lulled in a false sense of security when we only rely on unit testing, which makes it absolutely vital to have a proper integration test setup in place.

### How does it work?

There have quite a couple of changes been made to the existing code base for this example, which should help making the code more maintainable and having a more concise separation of concerns.

#### Adjusted Project Structure

There are now two new maven sub modules:

* todo-api - Contains the todo API and domain interfaces which have before been located in `todo-service/src/main/java/my/demo/springboot/microservice/todo/domain`.
This allows us to have the Todo Microservice implemented independently from the rest of the project
and at the same time provide a well-defined API for other consumers.
* todo-integrationtest - A separate project containing everything necessary for the integration test setup. This can also be implemented independently from the other projects.

Further explanations will mainly focus on the `todo-integrationtest` sub project.

#### Adding Spring Boot Actuator

For the integration test setup we need to be able to start the three services

* `eureka-service`
* `account-service`
* `todo-service`

manually and verify whether their endpoints return valid results. Therefore the Spring Boot Actuator framework needs to be included in all three project's pom files.

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

#### What will be tested?

In our test scenario we will first start all of the above mentioned services manually. This means that all of the functionality
we discussed so far will also be available when the services are running. There will be a running Eureka instance taking care of service discovery,
as well as Feign Client- and Ribbon load-balancing functionality.

This is great, because now we don't need to take care about mocking the critical services of our application stack where all sorts of things might go wrong.

Let's begin with the test cases. In `TodoApplicationIntegrationTest` a couple of tests have been defined in given-when-then semantics.
Before the tests are run, the services will be started in the test classes `setup` method. We use the `@BeforeClass` annotation, because we wouldn't want the services
to be started and shutdown for every new test method executed by JUnit, which would be the case if we used `@Before`. After all tests have been finished the services will be shutdown
again from the `taerDown` method, annotated by `@AfterClass`.

Since we also want to automatically test scenarios mentioned in the last section where for example a fallback should be called when the `account-service` instance is
not reachable anymore, the two methods `startIfDown` and `stopIfRunning` have been introduced. Those methods will guarantee that for any independent test scenario we defined
our services will be maintained the correct state (which means they are started or shutdown).

What does that mean. Let's look at the test with test signature `givenTodos_whenFindAllByWrongAccount_thenAccountNotFound`. Actually we want to see that when we are querying the `todo-service` endpoint
`/accounts/{accountid}/todos` with a wrong `accountid` the client should receive an error message containing the text `Account with id {id} not found`.
If we want this test to succeed we need to ensure that at least one instance of `account-service` is running. And that is what `startIfDown` is doing. In contrast there's the same requirement for
use cases where we want to test fallback functionality. In those cases we need to assure that no `account-service` instance is running and therefore would use `stopIfRunning`.

`spring-boot-microservice-demo/todo-integrationtest/src/test/java/my/demo/springboot/microservice/todo/it/TodoApplicationIntegrationTest.java`
```java
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
    public void givenTodos_whenFindAllByWrongAccount_thenAccountNotFound() {
        startIfDown(Instance.ACCOUNT);
        assertNotNull(ServiceEnvironment.getInstance(Instance.ACCOUNT));

        assertTrue(errorResponseFromTodoService().contains("Account with id 4e696b86-257f-4887-8bae-027d8e883637 not found"));
    }

    ...


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

    ...

    private void startIfDown(final Instance instance) {
        final SpringBootServiceWrapper service = ServiceEnvironment.getInstance(instance);
        if(service==null) {
            ServiceEnvironment.addAccountInstance().startService();
        }
    }

```

#### Integration Test Setup

The main building blocks of the integration test environment setup are the two classes `ServiceEnvironment` and `SpringBootServiceWrapper`.

`ServiceEnvironment` maintains a static map of the services that should be tested. It also provides static methods for starting and stopping the services.

`spring-boot-microservice-demo/todo-integrationtest/src/test/java/my/demo/springboot/microservice/todo/it/ServiceEnvironment.java`
```java
public class ServiceEnvironment {

    public static final String TODO_SERVICE_NAME="todo";
    public static final String TODO_SERVICE_VERSION="0.0.1-SNAPSHOT";
    public static final int TODO_SERVICE_PORT=8081;

    public static final String ACCOUNT_SERVICE_NAME="account";
    public static final String ACCOUNT_SERVICE_VERSION="0.0.1-SNAPSHOT";
    public static final int ACCOUNT_SERVICE_PORT=9090;

    public static final String EUREKA_SERVICE_NAME="eureka";
    public static final String EUREKA_SERVICE_VERSION="0.0.1-SNAPSHOT";
    public static final int EUREKA_SERVICE_PORT=8761;

    private static final Map<Instance, SpringBootServiceWrapper> instances = new LinkedHashMap<>();

    static {
        ServiceEnvironment.addEurekaInstance();
        ServiceEnvironment.addAccountInstance();
        ServiceEnvironment.addTodoInstance();
    }

    public static SpringBootServiceWrapper addAccountInstance() {
        ServiceEnvironment.instances
                .put(Instance.ACCOUNT, new SpringBootServiceWrapper(ServiceEnvironment.ACCOUNT_SERVICE_NAME,
                        ServiceEnvironment.ACCOUNT_SERVICE_VERSION, ServiceEnvironment.ACCOUNT_SERVICE_PORT));
        return ServiceEnvironment.instances.get(Instance.ACCOUNT);
    }

    public static SpringBootServiceWrapper addEurekaInstance() {
        ServiceEnvironment.instances
                .put(Instance.EUREKA, new SpringBootServiceWrapper(ServiceEnvironment.EUREKA_SERVICE_NAME,
                        ServiceEnvironment.EUREKA_SERVICE_VERSION, ServiceEnvironment.EUREKA_SERVICE_PORT));
        return ServiceEnvironment.instances.get(Instance.EUREKA);
    }

    public static SpringBootServiceWrapper addTodoInstance() {
        ServiceEnvironment.instances
                .put(Instance.TODO, new SpringBootServiceWrapper(ServiceEnvironment.TODO_SERVICE_NAME,
                        ServiceEnvironment.TODO_SERVICE_VERSION, ServiceEnvironment.TODO_SERVICE_PORT));
        return ServiceEnvironment.instances.get(Instance.TODO);
    }

    public static void startServices() {
        ServiceEnvironment.instances.values().forEach(springBootServiceWrapper -> { springBootServiceWrapper.startService(); });
    }

    public static void shutdownServices() {
        ServiceEnvironment.instances.values().forEach(springBootServiceWrapper -> { springBootServiceWrapper.stopService(); });
    }

    ...

```

`SpringBootServiceWrapper` contains the logic for starting and stopping services as separate operating system processes. Java's built-in `ProcessBuilder` functionality is used for this.
Also, it contains the logic to evaluate whether passed enpoints are currently available or not and additionally to wait for certain conditions to become true or false in a specified time frame.
For those kind of checks [awaitility](http://www.awaitility.org/) is used, a framework which comes in handy when testing asynchronous applications where thread handling and concurrency is an issue.

For example, in `SpringBootServiceWrapper` the method `stopService` will be calling the `actuator/shutdown` endpoint of its current service instance until the pre-defined callback method returns true.
This is expressed through awaitility's very intuitive dsl. Here we say that we will only wait for a pre-defined time span until the condition we pass to the `until` method must be fulfilled. If the
callback still returns false after the time has elapsed, an exception will be thrown.

`spring-boot-microservice-demo/todo-integrationtest/src/test/java/my/demo/springboot/microservice/todo/it/SpringBootServiceWrapper.java`
```
    public void stopService() {
        try {
            await().atMost(SpringBootServiceWrapper.MAX_ENDPOINT_SHUTDOWN_TIME, TimeUnit.SECONDS).until(endpointIsShutdown());
        } catch(final Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(String.format("couldn't stop %s-service", serviceName));
        }
        SpringBootServiceWrapper.logger.info(String.format("shutdown %s-service", serviceName));
    }

    ...

    private Callable<Boolean> endpointIsShutdown() {
        return new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    final RestTemplate restTemplate = new RestTemplate();
                    final String url = String.format("http://localhost:%s/actuator/shutdown", port);

                    final HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);

                    final HttpEntity<String> entity = new HttpEntity<>("", headers);

                    final ResponseEntity<JSONObject> response = restTemplate
                            .postForEntity(url, entity, JSONObject.class);

                    return response.getStatusCode().is2xxSuccessful();
                } catch(final Exception e) {
                    return false;
                }
            }
        };
    }

```

## How-to run the app

In addition to [How-to run](HOW-TO-RUN.md) the integration test suite can be run by simply typing

```
mvn integration-test
```

either in `spring-boot-microservice-demo` or in `spring-boot-microservice-demo/todo-integrtationtest`. Integration tests won't be run when a normal build is started,
they have to be run explicitly. This behavior is defined in the pom.xml of `todo-integrationtest`.

`spring-boot-microservice-demo/todo-integrationtest/pom.xml`
```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-surefire-plugin</artifactId>
            <version>2.22.0</version>
            <configuration>
                <skip>true</skip>
            </configuration>
            <executions>
                <execution>
                    <id>integration-test</id>
                    <phase>integration-test</phase>
                    <goals>
                        <goal>test</goal>
                    </goals>
                    <configuration>
                        <skip>false</skip>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

### Troubleshooting

#### Manual Service Shutdown

Depending on the machine the integration test suite is run it might come to errors when services
are started or shutdown due to the pre-defined timeout behavior in `spring-boot-microservice-demo/todo-integrationtest/src/test/java/my/demo/springboot/microservice/todo/it/SpringBootServiceWrapper.java`. It might happen then that a
service won't be shutdown properly and when the integration test is run again startup errors will occur.

If this happens there are two options to shutdown the running services:

1. Try to stop the service gracefully by using the service's actuator endpoint: `curl -X POST localhost:{$PORT}/actuator/shutdown -k`, where `$PORT` is the port the service has been started on.
2. Shutdown the service by killing the running process. First verify on a command line if the service is running: `lsof -i:{$PORT}`. 
   
   If the service is still running the output should be similar to
    ```
    COMMAND   PID    USER   FD   TYPE             DEVICE SIZE/OFF NODE NAME
    java    23648    mike   23u  IPv6 0xaf6191ec3d63742f      0t0  TCP *:8761 (LISTEN)
    ```
    Then kill the running process: `kill -9 23648`

#### Wrong Project Path

When the sample project has been cloned into a different location the integration test won't be able to locate the correct location of the root project path.
The path then needs to be adjusted in `PathUtils.java`.

`spring-boot-microservice-demo/todo-integrationtest/src/test/java/my/demo/springboot/microservice/todo/it/PathUtils.java`
```java
private static final String ROOT_PATH = "spring-boot-microservice-demo";
```

Of course those sort of variables need to be replaced with a config file or passed through environment variables in a real life scenario.

### Conclusion

Having an integration test environment in place which can automatically be built and run is a great benefit. It uncovers errors that will usually only
become visible through time-intensive explorative and manual testing efforts. Doesn't sound very agile, right?
