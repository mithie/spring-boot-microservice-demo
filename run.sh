#!/bin/bash

java -jar eureka-service/target/eureka-service-0.0.1-SNAPSHOT.jar &

sleep 10

java -jar account-service/target/account-service-0.0.1-SNAPSHOT.jar &
java -jar todo-service/target/todo-service-0.0.1-SNAPSHOT.jar &
