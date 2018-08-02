# 01_Initial_Boot_Setup

## Demonstrated Principle

Feature **01_Initial_Boot_Setup** just contains a simple project template that you will get when you create a
new Spring Boot app with [Spring Initializr](https://start.spring.io/). This is the best point to get you started
when a new Microservice should be created. This project template compiles and builds, but does not yet contain
any functionality and therefore is not very useful.

## Run the App

### Prerequisites

The application has been built and run with the following software.

- [Java JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
- [Apache Maven 3](https://maven.apache.org/download.cgi)
- [Git 2](https://git-scm.com/downloads)


#### Build the Microservice project

```
mvn clean package

...

[INFO] ------------------------------------------------------------------------
[INFO] Reactor Summary:
[INFO]
[INFO] spring-boot-microservice-demo ...................... SUCCESS [  0.203 s]
[INFO] account-service .................................... SUCCESS [  9.291 s]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 11.383 s
[INFO] Finished at: 2018-06-25T17:19:33+02:00
[INFO] Final Memory: 58M/514M
[INFO] ------------------------------------------------------------------------

```

Open a new shell in the projects's root folder and execute the run shell script.

```
sh run.sh
```

Wait a few seconds and verify if the services have been started properly.
```
ps |grep java

13048 ttys001    0:00.00 grep java
12992 ttys002    0:43.90 /usr/bin/java -jar account-service/target/account-service-0.0.1-SNAPSHOT.jar
```

The service can now be accessed through the endpoint mentioned under [Service Enpoints and URLs](#Service Enpoints and URLs)

To shutdown the application simply run the shutdown script in the shell you opened before. Verification of a successful shutdown can again be
done with the ps command from the previous step.
```
sh shutdown.sh
```

## Service Enpoints and URLs
Service | URI | HTTP Method
--- | --- | ---
Account Service | http://localhost:8081/accounts | GET