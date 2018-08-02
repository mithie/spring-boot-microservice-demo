# 02_First_Service

## Demonstrated Principle

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

## How-to run the app

The application can be run on a local machine with the included shell script or by starting the Account Service manually with the 
`java -jar` command line option.

See [How-to run](HOW-TO-RUN.md) for further details.