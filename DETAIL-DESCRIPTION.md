# Table of Contents
1. [Initial Boot Setup](#01_Initial_Boot_Setup)
2. [First Service](#02_First_Service)
3. [Service Discovery](#03_Service_Discovery)

# 01_Initial_Boot_Setup

Feature **01_Initial_Boot_Setup** just contains a simple project template that you will get when you create a
new Spring Boot app with [Spring Initializr](https://start.spring.io/). This is the best point to get you started
when a new Microservice should be created. This project template compiles and builds, but does not yet contain
any useful functionality. There's one RESTful service endpoint in `account-service` project returning a simple
String message when called.

# 02_First_Service <a name="02_First_Service"/>

Feature **02_First_Service** contains the Account Service, the first of the two Microservices. This service
simply manages user accounts in a CRUD based fashion. There's not yet any database access (even tough the use of
databases in Spring Boot is painfully simple as we will see later). For the moment a simple HashMap will be acting as an accont repository.
Nevertheless we should be aware that we already have a fully working web service including some of Spring Boot's already
built-in features like
- an executable jar with all relevant dependencies already included
- a pre-configured, embedded tomcat server
- no configuration necessary to get the application up and running
- an already production ready service (even though we wouldn't deploy such a service in production)

### How does it work?

The project is set up as a multi module maven project. In the project's parent we include the **spring-boot-starter-parent** which enables us
to access the necessary Spring Boot starter projects from within our sub modules.

`spring-boot-microservice-demo/pom.xml`
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>

	<groupId>my.demo.springboot.microservice</groupId>
	<artifactId>spring-boot-microservice-demo</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<packaging>pom</packaging>

	<name>spring-boot-microservice-demo</name>
	<description>Demo project for Spring Boot</description>

	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>2.0.2.RELEASE</version>
		<relativePath/> <!-- lookup parent from repository -->
	</parent>

	<properties>
		<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
		<project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
		<java.version>1.8</java.version>
	</properties>

	<modules>
		<module>account-service</module>
	</modules>

</project>
```

That's pretty much it. If we take a look into the sub module's pom file we can see that
**spring-boot-starter-web** and **spring-boot-starter-test** are included which enable
us to write a full-fledged REST application and also provide specific unit tests for it.

`spring-boot-microservice-demo/account-service/pom.xml`
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

Note that you don't need to take care about any kind of server setup, 3rd party library integration
for logging, testing, etc. All of this is already contained within the starter dependencies.

Our REST endoint is pretty simple and straigt forward.

`spring-boot-microservice-demo/src/main/java/my/demo/springboot/microservice/account/api/AccountController`
```java
@RestController
public class AccountController {

    @Autowired
    AccountService accountService;

    @RequestMapping("/accounts/{id}")
    public Account findById(@PathVariable Long id){
        return accountService.findById(id);
    }

    @RequestMapping("/accounts")
    public Collection<Account> findAll(){
        return accountService.findAll();
    }
}
```

We just use the relevant Spring Boot annotations for retrieving the data and that's it.
In this case the business logic is outsourced to a separate service which also includes
the hard coded account repository.

`spring-boot-microservice-demo/src/main/java/my/demo/springboot/microservice/account/domain/AccountService`
```java
@Service
public class AccountService {

    private static Map<Long, Account> accountRepository = null;


    static {
        Stream<String> accountStream = Stream.of("1,John,Doe,John.Doe@foo.bar", "2,Jane,Doe,Jane.Doe@foo.bar");
        accountRepository = accountStream.map(account -> {
                String[] info = account.split(",");
        return new Account(new Long(info[0]), info[1], info[2], info[3]);
        }).collect(Collectors.toMap(Account::getId, usr -> usr));
    }

    public Account findById(Long id) {
        return accountRepository.get(id);
    }
    public Collection<Account> findAll() {
        return accountRepository.values();
    }
}
```

The only thing left to do now is to tell Spring Boot to start our application which is usually done in a
separate class containing all the configuration work. This again is done in the form of Spring Boot annotations and bean initializations.

`spring-boot-microservice-demo/src/main/java/my/demo/springboot/microservice/account/AccountServiceApplication`
```java
@SpringBootApplication
public class AccountServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AccountServiceApplication.class, args);
	}
}
```

In this example Spring Boot is configured to start the application on port 8081 which is
done within an application property or yaml file. The application configuration is stored under
**src/main/resources/application.yml**.

`spring-boot-microservice-demo/src/main/resources/application.yml`
```
server:
  port: 8081
```

Well, great! We now implemented a running REST application in less than 5 minutes and without the hassle
of installing, configuring and setting up a whole server environment containing all the relevant libraries to get started.
Just follow the steps described in [Run the App](#Run the App) and open your web browser.
Type in **http://localhost:8081/accounts** and you shoud see a result similar to this one:

```
[{"id":1,"firstName":"John","lastName":"Doe","email":"John.Doe@foo.bar"},{"id":2,"firstName":"Jane","lastName":"Doe","email":"Jane.Doe@foo.bar"}]
```

### Don't forget to test

Let's spend two more minutes to test our service. Again, this is pretty simple and straight forward.

`spring-boot-microservice-demo/src/test/java/my/demo/springboot/microservice/account/AccountServiceApplicationTests`
```java
@RunWith(SpringRunner.class)
@WebMvcTest(AccountController.class)
public class AccountServiceApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private AccountService accountService;

	private final ObjectMapper mapper = new ObjectMapper();


	@Test
	public void testGetUserSuccess() throws Exception {

		Account user = new Account(new Long(1), "John", "Doe", "John.Doe@foo.bar");

		given(accountService.findById(new Long(1))).willReturn(user);

		String res = mockMvc.perform(get("/accounts/1")
				.content(mapper.writeValueAsString(user))
				.contentType(APPLICATION_JSON)
		).andExpect(status().isOk())
		 .andReturn()
		 .getResponse()
		 .getContentAsString();

		Account response = mapper.readValue(res, Account.class);

		assertEquals(user, response);
	}
}
```
In this test we want to see if the result from our service matches our expectations. In order
to do this an **Account** is created manually which resembles the first object from the account list
generated in the **AccountService** above. Since we only want to make sure that our controller behaves
properly, we don't need a full application context to be started for our test and therefore use the **@WebMvcTest**
annotation. This tells Spring Boot that we only want to test the web layer for testing which is pretty cool.

Also, we will mock our **AccountService** with Mockito and tell it that the **Account** object we created before should be
returned whenever a call to the **/accounts/1** endpoint is made. The result will be stored in a String, converted into an
object representation with the Jackson ObjectMapper facility and finally be compared to the expected **Account** object.

See [Spring Boot Testing](https://spring.io/guides/gs/testing-web/) for further details on how to test Spring Boot applications.

Very good! We just implemented a simple Microservice in no time.

# 03_Service_Discovery

**03_Service_Discovery** additionally contains the Todo Service, the second of our two Microservices. Todo Service will gather information
from Accouunt Service when for example a new Todo will be created. For the communication between the services [Spring's RestTemplate](https://spring.io/guides/gs/consuming-rest/)
is used in combination with [Spring Cloud Eureka](https://spring.io/guides/gs/service-registration-and-discovery/), a service discovery originally developed by Netflix.

Account Service and Todo Service both register with the service discovery during startup. After successful service registration the Todo Service is then able to resolve the hostname and port
of Account Service simply by asking for the other services name from Eureka.

This is a pretty straight forward and simple strategy for decoupling services, but there's still an explicit dependency
from Todo Service to Account Service and there is still a constraint on synchronous http mechanisms which has a couple of implications regarding elasticity and fault tolerance.
Fortunately there are better ways to decouple Microservices which we will see later.

### How does it work?

Our goal now is to have our Microservices registered at a central registry. This provides the following benefits in a large scaled Microservice environment:
* Host resolution of services will be facilitated
* Client-side load-balancing can be introduced
* Decoupling of service consumers and providers

First let's see how simple it is to setup and run a new Eureka service instance.

We create a new maven sub project and call it **eureka-service**. Then we add the Eureka dependencies to the project's pom file.

`spring-boot-microservice-demo/eureka-service/pom.xml`
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
    </dependency>
</dependencies>
```

Then, we will reference this new sub module in the project's parent pom.

`spring-boot-microservice-demo/pom.xml`
```xml
<modules>
    <module>eureka-service</module>
</modules>
```

In order to get the Eureka server up and running, a simple **@EnableEurekaServer** annotation needs to be added to the Spring Boot main application.

`spring-boot-microservice-demo/eureka-service/src/main/java/my/demo/springboot/microservice/eureka/EurekaApplication.java`
```java
@SpringBootApplication
@EnableEurekaServer
public class EurekaApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaApplication.class, args);
    }
}
```

Cool! The Eureka part is almost done. We still need to add a valid configuration for the service registry.

`spring-boot-microservice-demo/eureka-service/src/main/resources/application.yml`
```yaml
server:
  port: 8761

eureka:
  client:
    registerWithEureka: false
    fetchRegistry: false
```

This simply tells Eureka to start the service registry on port **8761** and not to register with itself what wouldn't make too much sense.

Finally we want to make our Microservices aware of the service discovery. Let's do just that.
First add the Eureka Client dependency to Account Service's pom.xml file.

`spring-boot-microservice-demo/account-service/pom.xml`
```xml{.line-numbers}
<dependencies>
    <dependency>
		<groupId>org.springframework.cloud</groupId>
		<artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
	</dependency>
<dependencies>
```

Then configure Eureka by defining the service url of the discovery client. **http://localhost:8761** will be used as default if not stated otherwise.

`spring-boot-microservice-demo/account-service/src/main/resources/bootstrap.yml`
```yml{.line-numbers}
spring:
  application:
    name: account-service

eureka:
  client:
    serviceUrl:
      defaultZone: http://${eureka.host:localhost}:${eureka.port:8761}/eureka/
```

Enable Eureka by adding the **EnableDiscoveryClient** annotation.

`spring-boot-microservice-demo/account-service/src/main/java/my/demo/springboot/microservice/account/AccountServiceApplication.java`
```java{.line-numbers}
@SpringBootApplication
@EnableDiscoveryClient
public class AccountServiceApplication {

	public static void main(String[] args) {
		run(AccountServiceApplication.class, args);
	}
}
```


Do exactly the same for Todo Service.

### Service-To-Service Communication

Let's head on to the interesting part. We will implement the Todo Service which allows a consuming client to query for all Todos of a given account id.
In addition it should be possible to create new Todos based upon an existing account id. Both use cases imply that there needs to be some sort of validation
of the account id before a new Todo can be created or before a list of an account's Todos can be retrieved. Remember that a Microservice should run within its
own bounded context. We do not want to mix up account logic with the management of Todos and vice versa. This brings us to the point where the Todo Service
needs to talk to the Account Service.

`spring-boot-microservice-demo/todo-service/src/main/java/my/demo/springboot/microservice/todo/domain/TodoService.java`
```java{.line-numbers}
@Service
public class TodoService {

    private Map<Long, List<Todo>> todoRepository = null;

    private final List<Todo> todos;

    @Autowired
    private AccountClient accountClient;

    public TodoService() {
        final Stream<String> todoStream = Stream.of("1,1,John.Doe@foo.bar,Clean Dishes,false", "2,1,John.Doe@foo.bar,Pay Bills,false", "3,2,Jane.Doe@foo.bar,Go Shopping,true");

        todos = todoStream.map(todo -> {
            String[] info = todo.split(",");
            return new Todo(new Long(info[0]), new Long(info[1]), info[2], info[3], Boolean.getBoolean(info[4]));
        }).collect(Collectors.toCollection(ArrayList::new));

        todoRepository = todos.stream()
                .collect(Collectors.groupingBy(Todo::getAccountId));

    }

    public List<Todo> findAllById(Long accountId) {
        if(!accountClient.isAccountValid(accountId)) {
            throw new IllegalArgumentException(String.format("Account with id %s does not exist!", accountId));
        }
        return todoRepository.get(accountId);
    }

    public List<Todo> findAll() {

        return todoRepository.entrySet().stream().flatMap(l -> l.getValue().stream()).collect(Collectors.toList());
    }

    public Todo addTodo(Todo todo) {

        List<Todo> todos = findAllById(todo.getAccountId());
        if(todos==null) {
            todos = new ArrayList<Todo>();
        }

        if(todos.stream().filter(t -> t.equals(todo)).count()==1) {
            throw new IllegalArgumentException("Todo " + todo + " already exists");
        }

        todoRepository.get(todo.getAccountId()).add(todo);

        return todo;
    }
}
```

The constructor of TodoService just creates sample Todo data and stores it in a local repository (let's ignore the missing database for the moment).
The interesting method is **findAllById** which calls the injected AccountClient's **isAccountValid** method.

`spring-boot-microservice-demo/todo-service/src/main/java/my/demo/springboot/microservice/todo/client/AccountClient.java`
```java{.line-numbers}
@Component
public class AccountClient {

    private final DiscoveryClient discoveryClient;

    @Autowired
    AccountClient(final DiscoveryClient discoveryClient) {
        this.discoveryClient=discoveryClient;
    }

    public URI getAccountUri(final Long accountId) {

        final ServiceInstance instance = discoveryClient.getInstances("account-service").get(0);
        if (instance == null)
            return null;

        return UriComponentsBuilder.fromHttpUrl( (instance.isSecure() ? "https://" : "http://") +
                instance.getHost() + ":" + instance.getPort() + "/accounts/{id}")
                .buildAndExpand(accountId).toUri();
    }

    public ResponseEntity<Account> getAccount(final URI accountUri) {
        final RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForEntity(accountUri, Account.class);
    }

    public boolean isAccountValid(final Long accountId) {
        final ResponseEntity<Account> entity = getAccount(getAccountUri(accountId));
        return entity.getStatusCode().is2xxSuccessful();
    }
}
```
**AccountClient** injects **DiscoveryClient** which allows us to resolve a registered service's host name and port by its name provided in the **bootstrap.yml** or **application.yml** file of that service.
In our case we are interested in the Account Service whose application name is defined as **account-service** in **spring-boot-microservice-demo/account-service/src/main/resources/bootstrap.yml**. We can now
dynamically build the URL for accessing the Account Service endpoint from the Todo Microservice. This is done by using Spring Boot's **RestTemplate**. Since we are only interested if whether the passed account id is
valid or not it is sufficient to check for an http 2xx status code.

Great! We've seen now how to use a service registry in order to realize service-to-service communication. But there are still a few drawbacks with this solution:
* The use of **RestTemplate** can become cumbersome since we need to mix up REST API code with business logic which is not a good practice
* When there is more than one running instance of a Microservice we would have to define which instance to call and when
* There are currently no strategies for what to do when a Microservice doesn't respond to incoming calls or when it fails completely. How should we react in an environment of distributed Services without compromising other services?

Fortunately there is a solution to those questions. We will see how to resolve them in the upcoming sections.
