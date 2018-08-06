# 05_Eureka_And_Ribbon

## Demonstrated Principle

Branch **05_Eureka_And_Ribbon** demonstrates client-side load-balancing with Spring Clound Ribbon in
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

## How-to run the app

See [How-to run](HOW-TO-RUN.md) for further details.
