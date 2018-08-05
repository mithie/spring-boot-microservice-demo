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
[
   {
      "id" : 1,
      "email" : "John.Doe@foo.bar",
      "lastName" : "Doe",
      "firstName" : "John"
   },
   {
      "email" : "Jane.Doe@foo.bar",
      "id" : 2,
      "lastName" : "Doe",
      "firstName" : "Jane"
   }
]
```

#### Get Account with specific Account id
```
curl http://localhost:8081/accounts/1 |json_pp
```

will return

```json
{
   "id" : 1,
   "lastName" : "Doe",
   "firstName" : "John",
   "email" : "John.Doe@foo.bar"
}
```
### Test Todo Service

#### Get all Todos
```
curl http://localhost:8082/todos |json_pp
```

will return

```json
[
   {
      "description" : "Clean Dishes",
      "completed" : false,
      "accountId" : 1,
      "email" : "John.Doe@foo.bar",
      "id" : 1
   },
   {
      "accountId" : 1,
      "email" : "John.Doe@foo.bar",
      "id" : 2,
      "description" : "Pay Bills",
      "completed" : false
   },
   {
      "description" : "Go Shopping",
      "completed" : false,
      "id" : 3,
      "email" : "Jane.Doe@foo.bar",
      "accountId" : 2
   }
]
```
#### Get Todos for a specific Account id
```
curl http://localhost:8082/accounts/1/todos |json_pp
```

will return

```json
[
   {
      "email" : "John.Doe@foo.bar",
      "completed" : false,
      "description" : "Clean Dishes",
      "accountId" : 1,
      "id" : 1
   },
   {
      "accountId" : 1,
      "id" : 2,
      "completed" : false,
      "description" : "Pay Bills",
      "email" : "John.Doe@foo.bar"
   }
]
```
#### Add a new Todo
```
curl -d '{"id":"3","accountId":"2","email":"Jane.Doe@foo.bar","description":"go to school","completed":"false"}' -H "Content-Type: application/json" -X POST http://localhost:8082/todos |json_pp
```

will return

```json
{
   "accountId" : 2,
   "email" : "Jane.Doe@foo.bar",
   "id" : 3,
   "completed" : false,
   "description" : "go to school"
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