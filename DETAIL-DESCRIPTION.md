# Table of Contents
1. [Initial Spring Boot Setup](#01_Initial_Boot_Setup)
2. [First Service](#02_First_Service)
3. [Service Discovery](#03_Service_Discovery)
4. [Hateoas](#04_Hateoas)
5. [Eureka And Ribbon](#05_Eureka_And_Ribbon)
6. [Feign And Hystrix](#06_Feign_And_Hystrix)
7. [Integration Testing](#07_Integration_Testing)

# 1. Initial Spring Boot Setup <a name="01_Initial_Boot_Setup"/>

Feature **01_Initial_Boot_Setup** just contains a simple project template that you will get when you create a
new Spring Boot app with [Spring Initializr](https://start.spring.io/). This is the best point to get you started
when a new Microservice should be created. This project template compiles and builds, but does not yet contain
any useful functionality. There's one RESTful service endpoint in the `account-service` project returning a simple
String message when called.

Details will be explained in the following section.

# 2. First Service <a name="02_First_Service"/>

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

# 3. Service Discovery <a name="03_Service_Discovery"/>

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

# 4. Hateoas <a name="04_Hateoas"/>

Section **04_Hateoas** demonstrates the use of [HATEOAS](https://spring.io/understanding/HATEOAS) (= Hypertext as the Engine of Application State) and enables a consumer of a service to dynamically
navigate through a RESTful API. This should be done right from the beginning of RESTful API design with regard to designing mature REST APIs.
The great advantage of this approach is that a client then only needs to know the initial URI of a service endpoint and can then dynamically
decide where to go next based on the hypermedia links provided by the service. Spring Boot uses [HAL](https://en.wikipedia.org/wiki/Hypertext_Application_Language) for defining hypermedia links and
ships with a great starter project which takes away a lot of the pain compared to when you'd have to do this by yourself.

### How does it work?

Creating solid RESTful APIs is not a trivial task if you want to do it right. There are lots of pitfalls and drawbacks, especially when you try to do
something beyond the usual CRUD stuff. Lots of people still design their RESTful APIs in RPC style which is common practice when developing SOAP based
Services. But this has nothing to do with RESTful design paradigms. However, there's a great [guide](https://martinfowler.com/articles/richardsonMaturityModel.html) which can help to avoid at least
the greatest mistakes.

HATEOAS means Hypertext as the Engine of Application State and is a quite powerful concept which allows dynamic redirecting of clients to other servers
without the need to change the client. The principle is comparable to a browser where you can click on a link and navigate through the application by following subsequent links without
the need to know where the servers are located. Spring Boot provides a starter project facilitating the use of HATEOAS.

#### Setup the environment

First we add the Spring Boot starter dependencies to the **account-service** and **todo-service** project pom files.

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-hateoas</artifactId>
    </dependency>
</dependencies>
```

Then let's look at the **TodoController**.

```java
@RequestMapping(path = "/accounts/{accountid}/todos", method = RequestMethod.GET, produces = "application/hal+json")
public ResponseEntity<Resources<TodoResource>> findAllByAccountId(@PathVariable("accountid") UUID accountId){
    List<Todo> todos = todoService.findAllById(accountId);

    final URI uri = ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();

    return ResponseEntity.ok(todoResources(todos));
}
```

Instead of simply returning a List of Todo objects we wrap the response in a **Resources** object which allows us to build the hyperlinks our client should follow.

```java
private Resources<TodoResource> todoResources(List<Todo> todos) {
    final List<TodoResource> todoResources = todos.stream().map(TodoResource::new).collect(Collectors.toList());

    final Resources<TodoResource> resources = new Resources(todoResources);

    final String uriString = ServletUriComponentsBuilder.fromCurrentRequest().build().toUriString();
    resources.add(linkTo(methodOn(TodoController.class).findAll()).withSelfRel());

    return resources;
}
```

Based upon the **TodoController** method **findAll** a new hyperlink will be created for every Todo telling the client exactly how the reference to this resource looks like and how it can be retrieved.
Looking at the response from a request to **accounts/4e696b86-257f-4887-8bae-027d8e883638/todos** we will see the following result.

```
curl http://localhost:8082/accounts/4e696b86-257f-4887-8bae-027d8e883638/todos |json_pp
```

```json
{
   "_embedded" : {
      "todoResourceList" : [
         {
            "todo" : {
               "completed" : false,
               "todoId" : "f85b1164-6bd6-4a74-9f01-d49d9802ff96",
               "description" : "Clean Dishes",
               "email" : "John.Doe@foo.bar",
               "accountId" : "4e696b86-257f-4887-8bae-027d8e883638"
            },
            "_links" : {
               "self" : {
                  "href" : "http://localhost:8082/accounts/4e696b86-257f-4887-8bae-027d8e883638/todos"
               }
            }
         }
      ]
   },
   "_links" : {
      "self" : {
         "href" : "http://localhost:8082/todos"
      }
   }
}
```

Now we see that for every Todo there is an **href** generated pointing the client exactly to this resource.

Also, have a look into the test for the newly adjusted service under `src/test/java/my/demo/springboot/microservice/todo/TodoServiceApplicationTests.java`

### Related Readings

A good article for getting started with Spring Boot and HATEOAS support can be found at [REST Hateoas](https://spring.io/guides/gs/rest-hateoas/)

As always are different opinions whether it is good or not to use HATEOAS. The following [article](https://medium.com/@andreasreiser94/why-hateoas-is-useless-and-what-that-means-for-rest-a65194471bc8) provides a more controversial discussion of this topic.
However, I would say that using HATEOAS with Spring Boot comes in quite handy and as long as no other Web API concept is used it provides good readability of an API.

# 5. Eureka And Ribbon <a name="05_Eureka_And_Ribbon"/>

Section **05_Eureka_And_Ribbon** demonstrates client-side load-balancing with Spring Clound Ribbon in
combination with Spring Cloud Eureka. In contrast to server-side load-balancing where a central load balancer (either hardware
or software) decides to which server requests should be forwarded a client-side load balancer can itself decide which
servers to call. This works either dynamically through a service discovery like Eureka or statically with a fixed set of
pre-defined server instances. In our example we will use Ribbon to dynamically resolve account service instances from Todo Service.

### How does it work?

In order to use Ribbon we first include the starter dependency in the pom file of the **todo-service** project.

`spring-boot-microservice-demo/todo-service/pom.xml`
```xml
<dependencies>
    ...
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-ribbon</artifactId>
    </dependency>
</dependencies>
```

We will now teach our service to use Ribbon by adding the **@RibbonClient** annotation to our Spring Boot application. We provide the name of the service we want to access with Ribbon and
a separate configuration class that might contain customizations of the load-balancing behavior.

`spring-boot-microservice-demo/todo-service/src/main/java/my/demo/springboot/microservice/todo/TodoServiceApplication.java`
```java
...
@SpringBootApplication
@EnableDiscoveryClient
@RibbonClient(name = "account", configuration = AccountConfiguration.class)
public class TodoServiceApplication {
    public static void main(final String[] args) {
        run(TodoServiceApplication.class, args);
    }
}
```

In the **AccountConfiguration** we can customize the load-balancing behavior, i.e. we explicitly tell Ribbon to use round robin as load-balancing rule.
Notice that since we are using the `ribbonPing` method we need to add a `RequestMapping` to the root path `/` within the `AccountController` class, so that Ribbon will be able to
find the running service instance when initiating a ping.

Have a look into [Spring Cloud Ribbon](https://cloud.spring.io/spring-cloud-netflix/multi/multi_spring-cloud-ribbon.html) for further details.


`spring-boot-microservice-demo/todo-service/src/main/java/my/demo/springboot/microservice/todo/client/AccountConfiguration.java`
```java
public class AccountConfiguration {
    @Autowired
    IClientConfig ribbonClientConfig;

    @Bean
    public IPing ribbonPing(IClientConfig config) {
        return new PingUrl();
    }

    @Bean
    public IRule ribbonRule(IClientConfig config) {
        return new RoundRobinRule();
    }
}
```

`spring-boot-microservice-demo/account-service/src/main/java/my/demo/springboot/microservice/account/api/AccountController.java`
```java
    @RequestMapping(value = "/")
    public String home() {
        return "OK";
    }
}
```

And finally we will adjust the service configuration file and tell the application to use Ribbon for a service with name **account**
(which is specified in `spring-boot-microservice-demo/account-service/src/main/resources/application.yml`).

Those are the settings we provide in the `application.yml` file of **todo-service**
* UseIPAddrForServer - we want to use IP addresses to resolve host instances
* DeploymentContextBasedVipAddresses - the name of the service whose instances we want to have resolved by Ribbon
* ServerListRefreshInterval - the interval in which server lists should be refreshed
* NIWSServerListClassName - get the server list from Eureka

`spring-boot-microservice-demo/todo-service/src/main/resources/application.yml`
```yaml
eureka:
  instance:
    hostname: todo-service
  client:
    registerWithEureka: true
    fetchRegistry: true
    serviceUrl:
      defaultZone: http://${eureka.host:localhost}:${eureka.port:8761}/eureka/

account:
  ribbon:
    UseIPAddrForServer: true
    DeploymentContextBasedVipAddresses: account
    ServerListRefreshInterval: 15000
    NIWSServerListClassName: com.netflix.niws.loadbalancer.DiscoveryEnabledNIWSServerList
```


Then in the **AccountClient** we inject Spring Cloud's **LoadBalancerClient** which enables us to dynamically resolve hostnames by their
name. Instead of choosing an instance of a service by ourselves like in the previous version of **AccountClient** Ribbon does this task for us now.
Before using Ribbon we had to manually choose the instance of the service we wanted to access:

```
final ServiceInstance instance = discoveryClient.getInstances("account-service").get(0);
```

Also, we use a custom Error Handler for **RestTemplate** since we want to get notified whenever a client-side error occured within another service.

`spring-boot-microservice-demo/todo-service/src/main/java/my/demo/springboot/microservice/todo/client/AccountClient.java`
```java
...

@LoadBalanced
@Bean
RestTemplate restTemplate() {
    return restTemplateBuilder.errorHandler(responseErrorHandler).build();
}

@Autowired
RestTemplate restTemplate;

@Autowired
AccountClient(
        final LoadBalancerClient loadBalancer, final RestTemplateBuilder restTemplateBuilder, final ClientResponseErrorHandler responseErrorHandler) {
    this.loadBalancer=loadBalancer;
    this.restTemplateBuilder=restTemplateBuilder;
    this.responseErrorHandler=responseErrorHandler;
}


public URI getAccountUri(final UUID accountId) {

    final ServiceInstance instance = loadBalancer.choose("account");
    if (instance == null) {
        return null;
    }

    log.info("Service called on host: {}, port: {}", instance.getHost(), instance.getPort());

    return UriComponentsBuilder.fromHttpUrl( (instance.isSecure() ? "https://" : "http://") +
            instance.getHost() + ":" + instance.getPort() + "/accounts/{id}")
            .buildAndExpand(accountId).toUri();
}
```

# 6. Feign And Hystrix <a name="06_Feign_And_Hystrix"/>

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

Next, let's investigate on how to write an automated integration test of what we got so far.

# 7. Integration Testing <a name="07_Integration_Testing"/>

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

There have been quite a couple of changes been made to the existing code base for this example, which should help making the code more maintainable and having a more concise separation of concerns.

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

Having an integration test environment in place which can automatically be built and run is a great benefit. It uncovers errors that will usually only become visible through time-intensive explorative and manual testing efforts. Doesn't sound very agile, right?
