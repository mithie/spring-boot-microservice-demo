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

#### Automated run.sh script

Open a new shell in the projects's root folder and execute the run shell script.

```
sh run.sh
```

Wait a few seconds and verify if the services have been started properly.
```
ps |grep java

13048 ttys001    0:00.00 grep java
12932 ttys002    0:43.43 /usr/bin/java -jar eureka-service/target/eureka-service-0.0.1-SNAPSHOT.jar
12992 ttys002    0:43.90 /usr/bin/java -jar account-service/target/account-service-0.0.1-SNAPSHOT.jar
12993 ttys002    0:44.06 /usr/bin/java -jar todo-service/target/todo-service-0.0.1-SNAPSHOT.jar
```

The services can now be accessed through the endpoints mentioned under [Service Enpoints and URLs](#Service Enpoints and URLs)

To shutdown the application simply run the shutdown script in the shell you opened before. Verification of a successful shutdown can again be
done with the ps command from the previous step.
```
sh shutdown.sh
```

#### Manual setup

You can also run each of the services manually in a custom shell. To do this open a shell for each of the three services.

`spring-boot-microservice-demo/eureka-service`
```
java -jar target/eureka-service-0.0.1-SNAPSHOT.jar
```

`spring-boot-microservice-demo/account-service`
```
java -jar target/account-service-0.0.1-SNAPSHOT.jar
```

`spring-boot-microservice-demo/todo-service`
```
java -jar target/todo-service-0.0.1-SNAPSHOT.jar
```

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
curl http://localhost:8082/todos |json_pp
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
curl http://localhost:8082/accounts/4e696b86-257f-4887-8bae-027d8e883638/todos |json_pp
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
curl -d '{"accountId":"a52dc637-d932-4998-bb00-fe7f248319fb","email":"Jane.Doe@foo.bar","description":"invite friends","completed":"false"}' -H "Content-Type: application/json" -X POST http://localhost:8082/todos |json_pp
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
Account Service | http://localhost:8081/accounts/{id} | GET | shows all accounts for a specific account id
Todo Service | http://localhost:8082/todos | GET | shows all existing todos
Todo Service | http://localhost:8082/todos | POST | endpoint for adding a new todo
Todo Service | http://localhost:8082/accounts/{accountid}/todos | GET | shows all todos for a specific account id
Eureka Dashboard | http://localhost:8761 | GET | show the Eureka dashboard