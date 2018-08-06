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
[INFO] spring-boot-microservice-demo ...................... SUCCESS [  0.203 s]
[INFO] account-service .................................... SUCCESS [  9.291 s]
[INFO] todo-service ....................................... SUCCESS [  0.825 s]
[INFO] eureka-service ..................................... SUCCESS [  0.308 s]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 11.383 s
[INFO] Finished at: 2018-06-25T17:19:33+02:00
[INFO] Final Memory: 58M/514M
[INFO] ------------------------------------------------------------------------

```


#### Manual setup

For testing purposes run each of the services manually in a custom shell. Since we want to test the load-balancing behavior of Ribbon
we will start two instances of account service running on different ports.

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

## Testing the application behavior

Since we are now using Ribbon as client-side load balancer we can test different runtime scenarios of our application stack.
We're especially interested in the application behavior when something happens to the Account Service in between its communication with Todo Service.

Let's try and examine the following scenarios:

1. Both Account Service instances are up and running. This means that Ribbon should consecutively call one of the two instances.
2. One Account Service instance goes down. We want to be sure that Ribbon automatically chooses the only remaining instance.
3. Both Account Service instances are down. This will return an exception, because we don't have any sort of fallback functionality at hand.

Ok, let's test each of those scenarios and begin with the first one:

### 1. Two-instance scenario
In this scenario both instances can be reached and should be called in traditional round robin fashion. To test this we open a new shell and execute the following
CURL command two times:

```
curl http://localhost:9090/accounts/4e696b86-257f-4887-8bae-027d8e883638/todos |json_pp
```

When we open the shell where the **todo-service** is running we should see a log output similar to the following:

```
NFLoadBalancer:name=account,current list of Servers=[10.159.144.117:8082, 10.159.144.117:8081],Load balancer stats=Zone stats: {defaultzone=[Zone:defaultzone;  Instance count:2;       Active connections count: 0;      Circuit breaker tripped count: 0;       Active connections per server: 0.0;]
},Server stats: [[Server:10.159.144.117:8082;   Zone:defaultZone;       Total Requests:0;       Successive connection failure:0;        Total blackout seconds:0;       Last connection made:Thu Jan 01 01:00:00 CET 1970;        First connection made: Thu Jan 01 01:00:00 CET 1970;    Active Connections:0;   total failure count in last (1000) msecs:0;     average resp time:0.0;  90 percentile resp time:0.0;      95 percentile resp time:0.0;    min resp time:0.0;      max resp time:0.0;      stddev resp time:0.0]
, [Server:10.159.144.117:8081;  Zone:defaultZone;       Total Requests:0;       Successive connection failure:0;        Total blackout seconds:0;       Last connection made:Thu Jan 01 01:00:00 CET 1970;        First connection made: Thu Jan 01 01:00:00 CET 1970;    Active Connections:0;   total failure count in last (1000) msecs:0;     average resp time:0.0;  90 percentile resp time:0.0;      95 percentile resp time:0.0;    min resp time:0.0;      max resp time:0.0;      stddev resp time:0.0]
]}ServerList:DiscoveryEnabledNIWSServerList:; clientName:account; Effective vipAddresses:account; isSecure:false; datacenter:null

2018-08-02 11:10:17.939  INFO 88681 --- [nio-9090-exec-1] tClient$$EnhancerBySpringCGLIB$$b5024786 : Service called on host: 10.159.144.117, port: 8081
2018-08-02 11:10:28.021  INFO 88681 --- [nio-9090-exec-2] tClient$$EnhancerBySpringCGLIB$$b5024786 : Service called on host: 10.159.144.117, port: 8082

```

Notice that ribbon automatically switches between the service instances running on port 8081 and 8082 which is pretty cool since it shows us that the load balancing is working without having us spent too much effort on that item.


### 2. One-instance scenario

Now let's try the next scenario. We will shutdown one of the two Account Service instances. As a result there shouldn't be any obvious changes visible to the consuming application since Ribbon will
recognize in the background that there's only one service instance remaining. If you try out the same CURL request as in step 1 you will see that service calls are transparently handled by Ribbon

### 3. No-instance scenario

Assuming there is currently no running instance of **account-service** available, how should our Todo Application react? The best of course would be if it still responds with a valid result. But since we didn't define a fallback behavior yet the call
will just fail and respond with an error message.

Let's try it out. First we will shut down the single remaining **account-service** instance and wait for a few seconds until the instance has been deregistered from Eureka. We then issue the same CURL command as we did before
and look at the log output of the **todo-service** instance.

```
{
   "timestamp" : "2018-08-02T09:17:26.231+0000",
   "status" : 500,
   "message" : "URI is required",
   "path" : "/accounts/4e696b86-257f-4887-8bae-027d8e883638/todos",
   "error" : "Internal Server Error"
}
```

As we can see an HTTP 500 error is returned without any further information about the originating cause of the exception. This is probably the worst thing that could happen, because the consuming service is left alone with an exception it cannot handle.
Fortunately we can do better and provide a more intuitive user experience by specifying a well-defined fallback behavior. **06_Feign_And_Hystrix** will extend this sample application and show a way to do just that.

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
   "_embedded" : {
      "accountResourceList" : [
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
               "accountId" : "4e696b86-257f-4887-8bae-027d8e883638",
               "firstName" : "John.Doe@foo.bar",
               "lastName" : "Clean Dishes",
               "email" : "false"
            }
         },
         {
            "account" : {
               "email" : "false",
               "lastName" : "Pay Bills",
               "accountId" : "a52dc637-d932-4998-bb00-fe7f248319fb",
               "firstName" : "Jane.Doe@foo.bar"
            },
            "_links" : {
               "accounts" : {
                  "href" : "http://localhost:8081/accounts"
               },
               "self" : {
                  "href" : "http://localhost:8081/accounts/a52dc637-d932-4998-bb00-fe7f248319fb"
               }
            }
         }
      ]
   },
   "_links" : {
      "self" : {
         "href" : "http://localhost:8081/accounts"
      }
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
   "account" : {
      "lastName" : "Clean Dishes",
      "email" : "false",
      "accountId" : "4e696b86-257f-4887-8bae-027d8e883638",
      "firstName" : "John.Doe@foo.bar"
   },
   "_links" : {
      "self" : {
         "href" : "http://localhost:8081/accounts/4e696b86-257f-4887-8bae-027d8e883638"
      },
      "accounts" : {
         "href" : "http://localhost:8081/accounts"
      }
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
   "_links" : {
      "self" : {
         "href" : "http://localhost:8082/todos"
      }
   },
   "_embedded" : {
      "todoResourceList" : [
         {
            "_links" : {
               "self" : {
                  "href" : "http://localhost:8082/accounts/4e696b86-257f-4887-8bae-027d8e883638/todos"
               }
            },
            "todo" : {
               "accountId" : "4e696b86-257f-4887-8bae-027d8e883638",
               "description" : "Clean Dishes",
               "todoId" : "f85b1164-6bd6-4a74-9f01-d49d9802ff96",
               "completed" : false,
               "email" : "John.Doe@foo.bar"
            }
         },
         {
            "todo" : {
               "description" : "Pay Bills",
               "accountId" : "a52dc637-d932-4998-bb00-fe7f248319fb",
               "todoId" : "d79bf376-fd65-418d-83f9-ee5dbf9fd331",
               "completed" : false,
               "email" : "Jane.Doe@foo.bar"
            },
            "_links" : {
               "self" : {
                  "href" : "http://localhost:8082/accounts/a52dc637-d932-4998-bb00-fe7f248319fb/todos"
               }
            }
         }
      ]
   }
}
]
```
#### Get Todos for a specific Account id
```
curl http://localhost:9090/accounts/4e696b86-257f-4887-8bae-027d8e883638/todos |json_pp
```

will return

```json
{
   "_embedded" : {
      "todoResourceList" : [
         {
            "_links" : {
               "self" : {
                  "href" : "http://localhost:8082/accounts/4e696b86-257f-4887-8bae-027d8e883638/todos"
               }
            },
            "todo" : {
               "todoId" : "f85b1164-6bd6-4a74-9f01-d49d9802ff96",
               "completed" : false,
               "email" : "John.Doe@foo.bar",
               "accountId" : "4e696b86-257f-4887-8bae-027d8e883638",
               "description" : "Clean Dishes"
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
#### Add a new Todo
```
curl -d '{"accountId":"a52dc637-d932-4998-bb00-fe7f248319fb","email":"Jane.Doe@foo.bar","description":"invite friends","completed":"false"}' -H "Content-Type: application/json" -X POST http://localhost:9090/todos |json_pp
```

will return

```json
   "_links" : {
      "self" : {
         "href" : "http://localhost:8082/accounts/a52dc637-d932-4998-bb00-fe7f248319fb/todos"
      }
   },
   "todo" : {
      "description" : "invite friends",
      "completed" : false,
      "email" : "Jane.Doe@foo.bar",
      "accountId" : "a52dc637-d932-4998-bb00-fe7f248319fb",
      "todoId" : "ea2d5bac-891f-49c3-8f9c-ff09d9d5072e"
   }
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
