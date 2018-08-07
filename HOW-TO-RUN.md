## Run the App

### Prerequisites
The application has been built and run with the following software.

#### Local setup
- [Java JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
- [Apache Maven 3](https://maven.apache.org/download.cgi)
- [Git 2](https://git-scm.com/downloads)


#### Build the Microservice projects

```
mvn clean package

...

[INFO] ------------------------------------------------------------------------
[INFO] Reactor Summary:
[INFO]
[INFO] spring-boot-microservice-demo ...................... SUCCESS [  0.244 s]
[INFO] todo-api ........................................... SUCCESS [  2.791 s]
[INFO] eureka-service ..................................... SUCCESS [  1.607 s]
[INFO] account-service .................................... SUCCESS [  7.532 s]
[INFO] todo-service ....................................... SUCCESS [ 19.473 s]
[INFO] todo-integrationtest ............................... SUCCESS [  0.304 s]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 33.416 s
[INFO] Finished at: 2018-08-06T15:47:32+02:00
[INFO] Final Memory: 74M/806M
[INFO] ------------------------------------------------------------------------


```


#### Manual setup

For testing purposes run each of the services manually in a custom shell.

`spring-boot-microservice-demo/eureka-service`
```
java -jar target/eureka-service-0.0.1-SNAPSHOT.jar
```

`spring-boot-microservice-demo/account-service`
```
java -jar -Dserver.port=8081 target/account-service-0.0.1-SNAPSHOT.jar
```

`spring-boot-microservice-demo/account-service`
```
java -jar -Dserver.port=8082 target/account-service-0.0.1-SNAPSHOT.jar
```

`spring-boot-microservice-demo/todo-service`
```
java -jar -Dserver.port=9090 target/todo-service-0.0.1-SNAPSHOT.jar
```

## Testing load-balancing and fallback behavior

Since we now have Feign, Hystrix and Sleuth on the run we can test different scenarios of our application stack.
We're especially interested in the application behavior when something unexpected happens to the Account Service.

Let's try and examine the following scenarios:

1. Both Account Service instances are up and running. This means that Ribbon should consecutively call one of the two instances.
2. One Account Service instance goes down. We want to be sure that Ribbon automatically chooses the only remaining instance.
3. Both Account Service instances are down. This means that our Fallback will be activated and try to ready relevant data from its internal cache. If that doesn't return a response either, an exception should be thrown.

Ok, let's test each of those scenarios and begin with the first one:

### 1. Two-instance scenario
In this scenario both instances can be reached and should be called in traditional round robin fashion. To test this we open a new shell and execute the following
CURL command two times:

```
curl http://localhost:9090/accounts/4e696b86-257f-4887-8bae-027d8e883638/todos |json_pp
```

When we open the shell where the **todo-service** is running we should see a log output similar to the following:

```
2018-07-31 18:17:45.920  INFO [todo-service,d44f1d0b212f360e,d44f1d0b212f360e,false] 21034 --- [nio-9090-exec-9] m.d.s.m.todo.api.TodoController          : findAllByAccountId(4e696b86-257f-4887-8bae-027d8e883638)
2018-07-31 18:17:45.920  INFO [todo-service,d44f1d0b212f360e,d44f1d0b212f360e,false] 21034 --- [nio-9090-exec-9] m.d.s.m.todo.client.AccountClient        : isAccountValid(4e696b86-257f-4887-8bae-027d8e883638)
2018-07-31 18:17:45.920  INFO [todo-service,d44f1d0b212f360e,d44f1d0b212f360e,false] 21034 --- [nio-9090-exec-9] m.d.s.m.todo.client.AccountClient        : logAccess(): Service account-service called on host: 192.168.178.52, port: 8081

2018-07-31 18:18:27.520  INFO [todo-service,a472b685c223982b,a472b685c223982b,false] 21034 --- [io-9090-exec-10] m.d.s.m.todo.api.TodoController          : findAllByAccountId(4e696b86-257f-4887-8bae-027d8e883638)
2018-07-31 18:18:27.520  INFO [todo-service,a472b685c223982b,a472b685c223982b,false] 21034 --- [io-9090-exec-10] m.d.s.m.todo.client.AccountClient        : isAccountValid(4e696b86-257f-4887-8bae-027d8e883638)
2018-07-31 18:18:27.521  INFO [todo-service,a472b685c223982b,a472b685c223982b,false] 21034 --- [io-9090-exec-10] m.d.s.m.todo.client.AccountClient        : logAccess(): Service account-service called on host: 192.168.178.52, port: 8082

```

Notice that ribbon automatically switches between the service instances running on port 8081 and 8082 which is pretty cool since it shows us that the load balancing is working without having us spent too much efforts on the item.
Also, when we have a more detailed look at the log output we can see the Sleuth Trace Id which is automatically generated on the initial call to TodoController. If we look at the first log message we see that a unique Trace Id `d44f1d0b212f360e` has been created for this and all subsequent calls.
If you look now into the shell of your Account Service which runs on port 8081 and search for this id you should see a log message like the one below telling you that this Trace Id is directly related to the call of the **findById** method of Account Service.
This is really awesome since it dramatically facilitates debugging of your distributed application.

```
2018-07-31 18:17:45.928  INFO [account-service,d44f1d0b212f360e,a8c09001bec9716d,false] 21032 --- [nio-8081-exec-9] m.d.s.m.account.api.AccountController    : findById(4e696b86-257f-4887-8bae-027d8e883638)
```


### 2. One-instance scenario

Now let's try the next scenario. We will shutdown one of the two Account Service instances. As a result there shouldn't be any obvious changes visible to the consuming application since Ribbon will
recognize in the background that there's only one service available. If you try out the same CURL request as in 1. you will see that service calls are transparently handled by Ribbon. This behavior is also pretty cool since it guarantees our
consuming services that the will always get a response as long as at least one service instance is running.

### 3. No-instance scenario

Assuming there is currently no running instance of Account Service available, how should our Todo Application react? The best of course would be if it still responds with a valid result. We have seen before how we can achieve such behavior
with a Fallback defined in the Feign Service Client. Let's try it out now in real life.

First we will shut down the single remaining **account-service** instance and wait for a few seconds until the instance has been deregistered from Eureka. We then issue the same CURL command as we did before
and look at the log output of the **todo-service** instance.

This should give us something like:

```
2018-07-31 19:51:59.971  INFO [todo-service,ae601e361fe0e1db,ae601e361fe0e1db,false] 22151 --- [nio-9090-exec-2] m.d.s.m.todo.api.TodoController          : findAllByAccountId(4e696b86-257f-4887-8bae-027d8e883638)
2018-07-31 19:51:59.971  INFO [todo-service,ae601e361fe0e1db,ae601e361fe0e1db,false] 22151 --- [nio-9090-exec-2] m.d.s.m.todo.client.AccountClient        : isAccountValid(4e696b86-257f-4887-8bae-027d8e883638)
2018-07-31 19:51:59.971  WARN [todo-service,ae601e361fe0e1db,ae601e361fe0e1db,false] 22151 --- [nio-9090-exec-2] m.d.s.m.todo.client.AccountClient        : logAccess(): No services available!
2018-07-31 19:51:59.974  WARN [todo-service,ae601e361fe0e1db,7cbde4390d899d78,false] 22151 --- [count-service-2] m.d.s.m.t.client.AccountFallbackFactory  : findById(4e696b86-257f-4887-8bae-027d8e883638)
```

Great! Now we are still able to respond to **todo-service** even though no instance of **account-service** is running. The data simply is taken from the cache referenced from within our Fallback (of course a production like cache would not just be a simple HashMap!).
If you look one more time at the log output you will again see the Trace Id generated by Sleuth guiding us all the way through the call stack of our service calls. This is a real great feature, because it facilitates debugging tremendously.

There are still some other possible scenarios for testing, especially the ones which cause **todo-service** to respond with an exception to the consuming client. Think of what happens if no **account-service** instance is running and the Fallback
also cannot find the requested data. Then **todo-service** should get back to the consumer with a reasonable error message.

As you can see, performing manual integration tests can be quite cumbersome and time consuming. An option for automated integration testing will be introduced in branch **07_Integration_Testing**.

## Testing the endpoints

Testing the endpoints is quite easy and can be done in a couple of different ways. For the sake of simplicity we will use CURL to validate
the correctness of the services we created.

### Test Account Service

#### Get all Accounts
```
curl http://localhost:8081/accounts |json_pp
```

will return

```json
{
   "_links" : {
      "self" : {
         "href" : "http://localhost:8081/accounts"
      }
   },
   "_embedded" : {
      "accountResourceList" : [
         {
            "_links" : {
               "self" : {
                  "href" : "http://localhost:8081/accounts/4e696b86-257f-4887-8bae-027d8e883638"
               },
               "accounts" : {
                  "href" : "http://localhost:8081/accounts"
               }
            },
            "account" : {
               "lastName" : "Doe",
               "accountId" : "4e696b86-257f-4887-8bae-027d8e883638",
               "firstName" : "John",
               "email" : "John.Doe@foo.bar"
            }
         },
         {
            "_links" : {
               "accounts" : {
                  "href" : "http://localhost:8081/accounts"
               },
               "self" : {
                  "href" : "http://localhost:8081/accounts/a52dc637-d932-4998-bb00-fe7f248319fb"
               }
            },
            "account" : {
               "lastName" : "Doe",
               "accountId" : "a52dc637-d932-4998-bb00-fe7f248319fb",
               "email" : "Jane.Doe@foo.bar",
               "firstName" : "Jane"
            }
         }
      ]
   }
}
```

#### Get Account with specific Account id
```
curl http://localhost:8081/accounts/4e696b86-257f-4887-8bae-027d8e883638 |json_pp
```

will return

```json
{
   "_links" : {
      "accounts" : {
         "href" : "http://localhost:8081/accounts"
      },
      "self" : {
         "href" : "http://localhost:8081/accounts/4e696b86-257f-4887-8bae-027d8e883638"
      }
   },
   "account" : {
      "email" : "John.Doe@foo.bar",
      "lastName" : "Doe",
      "firstName" : "John",
      "accountId" : "4e696b86-257f-4887-8bae-027d8e883638"
   }
}
```
### Test Todo Service

#### Get all Todos
```
curl http://localhost:9090/todos |json_pp
```

will return

```json
{
   "_embedded" : {
      "todos" : [
         {
            "completed" : false,
            "_links" : {
               "self" : {
                  "href" : "http://localhost:9090/todos/662d0c82-507d-444f-ab70-4b9338959645"
               },
               "accountTodos" : {
                  "href" : "http://localhost:9090/accounts/4e696b86-257f-4887-8bae-027d8e883638/todos"
               },
               "todos" : {
                  "href" : "http://localhost:9090/todos"
               }
            },
            "todoId" : "662d0c82-507d-444f-ab70-4b9338959645",
            "description" : "Clean Dishes",
            "email" : "John.Doe@foo.bar",
            "accountId" : "4e696b86-257f-4887-8bae-027d8e883638"
         },
         {
            "description" : "Watch NBA",
            "email" : "John.Doe@foo.bar",
            "accountId" : "4e696b86-257f-4887-8bae-027d8e883638",
            "completed" : false,
            "_links" : {
               "todos" : {
                  "href" : "http://localhost:9090/todos"
               },
               "self" : {
                  "href" : "http://localhost:9090/todos/8f6bb6bc-7f17-4cb1-9735-1e913220bcc1"
               },
               "accountTodos" : {
                  "href" : "http://localhost:9090/accounts/4e696b86-257f-4887-8bae-027d8e883638/todos"
               }
            },
            "todoId" : "8f6bb6bc-7f17-4cb1-9735-1e913220bcc1"
         },
         {
            "accountId" : "a52dc637-d932-4998-bb00-fe7f248319fb",
            "email" : "Jane.Doe@foo.bar",
            "description" : "Pay Bills",
            "todoId" : "b7e5354a-8348-4f0b-86f1-c982c541e3e3",
            "_links" : {
               "self" : {
                  "href" : "http://localhost:9090/todos/b7e5354a-8348-4f0b-86f1-c982c541e3e3"
               },
               "accountTodos" : {
                  "href" : "http://localhost:9090/accounts/a52dc637-d932-4998-bb00-fe7f248319fb/todos"
               },
               "todos" : {
                  "href" : "http://localhost:9090/todos"
               }
            },
            "completed" : false
         }
      ]
   }
}
```
#### Get Todos for a specific Account id
```
curl http://localhost:9090/accounts/4e696b86-257f-4887-8bae-027d8e883638/todos |json_pp
```

will return

```json
{
   "_embedded" : {
      "todos" : [
         {
            "description" : "Clean Dishes",
            "completed" : false,
            "_links" : {
               "todos" : {
                  "href" : "http://localhost:9090/todos"
               },
               "self" : {
                  "href" : "http://localhost:9090/todos/662d0c82-507d-444f-ab70-4b9338959645"
               },
               "accountTodos" : {
                  "href" : "http://localhost:9090/accounts/4e696b86-257f-4887-8bae-027d8e883638/todos"
               }
            },
            "accountId" : "4e696b86-257f-4887-8bae-027d8e883638",
            "email" : "John.Doe@foo.bar",
            "todoId" : "662d0c82-507d-444f-ab70-4b9338959645"
         },
         {
            "completed" : false,
            "description" : "Watch NBA",
            "_links" : {
               "accountTodos" : {
                  "href" : "http://localhost:9090/accounts/4e696b86-257f-4887-8bae-027d8e883638/todos"
               },
               "self" : {
                  "href" : "http://localhost:9090/todos/8f6bb6bc-7f17-4cb1-9735-1e913220bcc1"
               },
               "todos" : {
                  "href" : "http://localhost:9090/todos"
               }
            },
            "email" : "John.Doe@foo.bar",
            "accountId" : "4e696b86-257f-4887-8bae-027d8e883638",
            "todoId" : "8f6bb6bc-7f17-4cb1-9735-1e913220bcc1"
         }
      ]
   }
}
```
#### Add a new Todo
```
curl -d '{"accountId":"a52dc637-d932-4998-bb00-fe7f248319fb","email":"Jane.Doe@foo.bar","description":"invite friends","completed":"false"}' -H "Content-Type: application/json" -X POST http://localhost:9090/todos |json_pp
```

will return

```json
{
   "_links" : {
      "accountTodos" : {
         "href" : "http://localhost:9090/accounts/a52dc637-d932-4998-bb00-fe7f248319fb/todos"
      },
      "todos" : {
         "href" : "http://localhost:9090/todos"
      },
      "self" : {
         "href" : "http://localhost:9090/todos/423d933d-638e-406a-b07f-a4c40ea25fbc"
      }
   },
   "email" : "Jane.Doe@foo.bar",
   "completed" : false,
   "accountId" : "a52dc637-d932-4998-bb00-fe7f248319fb",
   "todoId" : "423d933d-638e-406a-b07f-a4c40ea25fbc",
   "description" : "invite friends"
}
```

## Service Enpoints and URLs

Service | URI | HTTP Method | Description
--- | --- | --- | ---
Account Service | http://localhost:8081/accounts | GET | shows all existing accounts
Account Service | http://localhost:8082/accounts | GET | -
Account Service | http://localhost:8081/accounts/{id} | GET | shows all accounts for a specific account id
Account Service | http://localhost:8082/accounts/{id} | GET | -
Todo Service | http://localhost:9090/todos | GET | shows all existing todos
Todo Service | http://localhost:9090/todos | POST | endpoint for adding a new todo
Todo Service | http://localhost:9090/accounts/{accountid}/todos | GET | shows all todos for a specific account id
Eureka Dashboard | http://localhost:8761 | GET | show the Eureka dashboard