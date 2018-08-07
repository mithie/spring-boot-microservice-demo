# 06_Feign_And_Hystrix

## Demonstrated Principle

**06_Feign_And_Hystrix** demonstrates the use of Feign, a declarative Web Client library which allows it to easily
call remote REST endpoints through a simple interface. The great benefit when using Feign is that Ribbon is already built-in which
makes the code much cleaner and easier to use. There's no need anymore for using `RestTemplate`. Additionally, we will be using Feign in combination
with Hystrix, a Circuit Breaker originally developed by Netflix allowing us to implement sophisticated fallback behavior when
services are not available or under high load.

We will add one additional and very useful framework in this project. Since we are developing Microservices we will soon have the need to debug our
application stack and find unforeseen errors. This can become very painful in distributed environments and therefore we will use Spring Cloud Sleuth as
distributed tracing solution for our app stack. Whenever a service call is initiated from a client Sleuth will span a unique trace id which will be the same
for the whole call chain of our services. This allows us to trace over multiple service calls by just identifying and comparing the trace id.

### How does it work?

#### Configure account-service

Add the following to the **account-service** pom.xml:

`spring-boot-microservice-demo/account-service/pom.xml`
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-hystrix-dashboard</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-sleuth</artifactId>
</dependency>
```

Now let's configure the **application.yml** in **account-service**

The configuration settings below define a logging pattern which we will need for the use of Sleuth.
Also, we adjust Eureka's **leaseRenewalIntervalInSeconds** and **leaseExpirationDurationInSeconds** properties to override the default settings
with shorter time spans. And finally we tell Ribbon that Eureka should be enabled for service discovery.

`spring-boot-microservice-demo/account-service/src/main/resources/application.yml`
```yaml
spring:
  application:
    name: account-service

  logging:
    pattern:
      console: "%clr(%d{yyyy-MM-dd HH:mm:ss}){faint} %clr(${LOG_LEVEL_PATTERN:-%5p}) %clr([${springAppName:-},%X{X-B3-TraceId:-},%X{X-B3-SpanId:-},%X{X-Span-Export:-}]){yellow} %clr(${PID:- }){magenta} %clr(---){faint} %clr([%15.15t]){faint} %clr(%-40.40logger{39}){cyan} %clr(:){faint} %m%n${LOG_EXCEPTION_CONVERSION_WORD:-%wEx}"
    level:
      my.demo.springboot: DEBUG

eureka:
  instance:
    leaseRenewalIntervalInSeconds: 1
    leaseExpirationDurationInSeconds: 2
  client:
    serviceUrl:
      defaultZone: http://${eureka.host:localhost}:${eureka.port:8761}/eureka/

ribbon:
  eureka:
    enabled: true
```

Finally we have to make our application aware of the new features we want to use.

`spring-boot-microservice-demo/account-service/src/main/java/my/demo/springboot/microservice/account/AccountServiceApplication.java`
```java
@SpringBootApplication
@EnableEurekaClient
@EnableCircuitBreaker
@EnableHystrixDashboard
public class AccountServiceApplication {

	@Bean
	AccountConfiguration accountConfiguration() {
		return new AccountConfiguration();
	}

	public static void main(final String[] args) {
		run(AccountServiceApplication.class, args);
	}
}
```

#### Configure todo-service

Let's start with the pom.xml again. For **todo-service** we add additional dependencies for testing the feign client.

`spring-boot-microservice-demo/todo-service/pom.xml`
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-sleuth</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-hystrix-dashboard</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-contract-stub-runner</artifactId>
</dependency>
```

In **application.yml** we only have to add Feign support for our application.

`spring-boot-microservice-demo/todo-service/src/main/resources/application.yml`
```yaml
feign:
  hystrix:
    enabled: true
```

And then again we have to update our Spring Boot application with the necessary annotations.

`spring-boot-microservice-demo/todo-service/src/main/java/my/demo/springboot/microservice/todo/TodoServiceApplication.java`
```java
@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients
@EnableCircuitBreaker
public class TodoServiceApplication {

    @Bean
    TodoConfiguration todoConfiguration() {
        return new TodoConfiguration();
    }

    public static void main(final String[] args) {
        run(TodoServiceApplication.class, args);
    }
}
```

That's pretty much all we have to do in order to use Feign, Hystrix and Sleuth within our services. You might have noticed that adding new tooling always follows the same pattern:
* Add the relevant Spring Boot / Spring Cloud starter dependencies to a project's pom file
* Adjust the application configuration when necessary
* Add relevant annotations to the Spring Boot main application class

#### Implement a Feign Client

Having the configuration in place let's now move on to the fun part and implement a Feign Client interface for the communication between **todo-service** and **account-service**.
A Feign client is really nothing more than an interface annotated with `@FeignClient`. In addition the client will be provided with a name attribute, which will automatically create Ribbon load-balancing support for the corresponding service specified under that name.
With the `fallbackFactory` attribute an interface is defined providing custom defined fallback mechanisms.

`spring-boot-microservice-demo/todo-service/src/main/java/my/demo/springboot/microservice/todo/client/AccountProxy.java`
```java
package my.demo.springboot.microservice.todo.client;

@FeignClient(name = "account-service", fallbackFactory = AccountFallbackFactory.class)
public interface AccountProxy {

    @RequestMapping(value = "/accounts/{id}", produces = "application/hal+json", method= RequestMethod.GET)
    public ResponseEntity<Account> findById(@PathVariable final UUID id);
}
```

Looking at the `FallbackFactory` implementation a few noteworthy things can be observed.

* Instead of the `fallbackFactory` attribute we could also have used the `fallback` attribute, but since we're interested in the exception which caused our callback to be initiated we use the factory instead.
* When the `fallbackFactory` recognizes that there are no instances of `account-service` running anymore, the fallback functionality will be called. In this case a local cache will be asked if it knows the account id passed to the Feign client method `findById`.
If the account isn't known in the cache either, an exception will be thrown.

In this scenario we have full controll over the fallback behavior of our service. This way exceptions like the http 500 error can be circumvented and be replaced with something more meaningful.

Note that in a production like service a real cache implementation like Redis or Infinispan would be used of course. This sample only serves for demonstration.

`spring-boot-microservice-demo/todo-service/src/main/java/my/demo/springboot/microservice/todo/client/AccountFallbackFactory.java`
```java
@Component
public class AccountFallbackFactory implements FallbackFactory<AccountProxy>{

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    TodoConfiguration todoConfiguration;

    @Autowired
    private LoadBalancerClient loadBalancerClient;

    @Override
    public AccountProxy create(final Throwable throwable) {
        return new AccountProxy() {
            @Override
            public ResponseEntity<Account> findById(final UUID id) {
                logger.warn(String.format("findById(%s)", id));

                if (loadBalancerClient.choose("account-service") == null) {
                    if (localFakeCache().get(id) != null) {
                        return ResponseEntity.ok(new Account(id, null, null, null));
                    } else {
                        throw new IllegalArgumentException(String.format("Account with Id %s not found in cache.", id));
                    }
                }
                throw new IllegalArgumentException(throwable);
            }
        };
    }

    private Map<UUID, Todo> localFakeCache() {
        final UUID accountOneId = todoConfiguration.getAccountOneId();
        final UUID accountTwoId = todoConfiguration.getAccountTwoId();

        final Map<UUID, Todo> fallbackMap = new ConcurrentHashMap<>();

        fallbackMap.put(accountOneId, todoConfiguration.todoRepository().get(accountOneId).get(0));
        fallbackMap.put(accountTwoId, todoConfiguration.todoRepository().get(accountTwoId).get(0));

        return fallbackMap;
    }
}
```

Also, a look into the `AccountClient` shows that this class has become very simple and could ideally be removed completely. It's just left over for convenience and to demonstrate
what has become of the functionality of calling the Feign Client method. Finally there's no more need for load-balancing code within the business logic which is a great step forward in regards of
separation of concerns.

`spring-boot-microservice-demo/todo-service/src/main/java/my/demo/springboot/microservice/todo/client/AccountClient.java`
```java
@Component
public class AccountClient {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private LoadBalancerClient loadBalancerClient;

    @Autowired
    AccountProxy accountProxy;

    public boolean isAccountValid(final UUID accountId) {
        logger.info(String.format("isAccountValid(%s)", accountId));

        logAccess();

        return accountProxy.findById(accountId).getStatusCode().is2xxSuccessful();
    }

    private void logAccess() {
        ServiceInstance instance = loadBalancerClient.choose("account-service");

        if(instance != null) {
            logger.info("logAccess(): Service {} called on host: {}, port: {}", instance.getServiceId(), instance.getHost(), instance.getPort());
        } else {
            logger.warn("logAccess(): No services available!");
        }
    }
}
```

One more thing to notice is the way how exceptions are handled. Since we are using `fallbackFactory` where we have full control over the causing exception we also want to provide a concise error message to the consumer.
This is possible by implementing Spring's `ResponseEntityExceptionHandler` and within there use a custom error message format which can simply be modelled as Pojo. This Pojo will then automatically be serialized into a json representation
and sent back to the consumer in a more readable and understandable format. Since `AccountFallbackFactory` throws a `HystrixRuntimeException` when the fallback factory itself fails this can now be handled in the error handler and as such
it is guaranteed that the consumer always receives an error result in a pre-defined standard format.

`spring-boot-microservice-demo/todo-service/src/main/java/my/demo/springboot/microservice/todo/exception/ErrorHandler.java`
```java
@ControllerAdvice
@RestController
public class ErrorHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public final ResponseEntity<ErrorResult> handleIllegalArgumetException(IllegalArgumentException exception, WebRequest request) {
        ErrorResult result = new ErrorResult(new Date(), exception.getCause().getMessage(), request.getDescription(false));
        return new ResponseEntity<>(result, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(HystrixRuntimeException.class)
    public final ResponseEntity<ErrorResult> handleHystrixRuntimeException(HystrixRuntimeException exception, WebRequest request) {
        ErrorResult result = new ErrorResult(new Date(), exception.getFallbackException().getCause().getMessage(), request.getDescription(false));
        return new ResponseEntity<>(result, HttpStatus.NOT_FOUND);
    }

}
```

`spring-boot-microservice-demo/todo-service/src/main/java/my/demo/springboot/microservice/todo/exception/ErrorResult.java`
```java
@Data
@AllArgsConstructor
public class ErrorResult {
    private Date timestamp;
    private String message;
    private String details;
}
```

#### Testing Feign with Wiremock

For testing the Feign client in **todo-service**, [wiremock](http://wiremock.org) will be used. Wiremock is a mock framework which facilitates HTTP-based API testing and can easily be
integrated with Spring Boot applications (see [Spring Cloud Contract WireMock](http://cloud.spring.io/spring-cloud-static/spring-cloud-contract/1.1.2.RELEASE/#_spring_cloud_contract_wiremock) for further details).

First, the necessary dependencies need to be included.

`spring-boot-microservice-demo/todo-service/pom.xml`
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-contract-stub-runner</artifactId>
</dependency>
```

The test itself is straight forward. Eureka will be disabled in this scenario since wiremock takes care of requests and responses in the background.
Typically responses will be stubbed in wiremock, which can be seen in the `testFindById` test method. The call of the given endpoint within this method will
as result return the content provided in `account.json` located in the test's `resources/__files` directory.

`spring-boot-microservice-demo/todo-service/src/test/java/my/demo/springboot/microservice/todo/api/TodoFeignApplicationTest.java`
```java
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "feign.hystrix.enabled=true",
        "eureka.client.enabled=false"
})
@ContextConfiguration(classes = {TodoFeignApplicationTest.TodoServiceTestConfiguration.class})
public class TodoFeignApplicationTest {

    @ClassRule
    public static WireMockClassRule wiremock = new WireMockClassRule(
            wireMockConfig().dynamicPort());

    ...

    @Test
    public void testFindById() {
        stubFor(get(urlEqualTo("/accounts/" + testConfig.getAccountId()))
            .willReturn(aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withHeader("Content-Type", "application/hal+json")
                    .withBodyFile("account.json")));

        final ResponseEntity<Account> account = accountProxy.findById(testConfig.getAccountId());

        assertNotNull("should not be null", account);
        assertThat(account.getBody().getAccountId(), is(testConfig.getAccountId()));
        assertThat(account.getBody().getFirstName(), is("John"));
        assertThat(account.getBody().getLastName(), is("Doe"));
        assertThat(account.getBody().getEmail(), is("John.Doe@foo.bar"));
    }
}
```

### Conclusion

At this point we reached quite a good initial setup for our Microservice development. However, some very important things, like database support, app virtualization, a central configuration facility, distributed monitoring, API Gateway functionality, security,
event driven service communication, integration testing and continuous integration have not yet been addressed and still need to be covered in more detail.

Next, let's investigate in **07_Integration_Testing** on how to write an automated integration test of what we got so far.

## How-to run the app

See [How-to run](HOW-TO-RUN.md) for further details.
