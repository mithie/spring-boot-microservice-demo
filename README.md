# 04_Hateoas

## Demonstrated Principle

Item **04_Hateoas** demonstrates the use of [HATEOAS](https://spring.io/understanding/HATEOAS) (= Hypertext as the Engine of Application State) and enables a consumer of a service to dynamically
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

## How-to run the app

See [How-to run](HOW-TO-RUN.md) for further details.
