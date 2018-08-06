[![Build Status](https://travis-ci.com/mithie/spring-boot-microservice-demo.svg?branch=master)](https://travis-ci.com/mithie/spring-boot-microservice-demo)
[![MIT License](https://img.shields.io/badge/license-MIT%20License-blue.svg)](https://github.com/mithie/spring-boot-microservice-demo/blob/master/LICENSE)

# Spring Boot Microservice Demo 

This project demonstrates the usage of Spring Boot and Spring Cloud for creating a simple cloud-native eco system.
The concepts shown use several [Spring Boot starters](https://docs.spring.io/spring-boot/docs/current-SNAPSHOT/reference/htmlsingle/#using-boot-starter) which facilitate the creation of Microservice based applications. The features included in this application scenario are outlined below.

To keep things simple there will only be two services interacting with each other for the time being.

- Account Service - A service managing account functionality of a user.
- Todo Service - A simple service managing todos. A Todo will be related to a user's account which makes it necessary for the two services to exchange data.

This repository contains several consecutive git branches where each branch demonstrates a single principle (see [Demonstrated Principles](#demonstrated-principles)) of a Microservice application stack. In order to run the complete stack including all of the explained features in the sub branches you need to check out and build the master branch.

***Note*** that detail descriptions are contained in each sub branch's README.md as well as in the [Detail Description](DETAIL-DESCRIPTION.md) section.

Git sub branches are named with a trailing two-digit number followed by the branch name and highlighted in **bold** font.

Currently the following branches can be checked out separately:
* **01_Initial_Boot_Setup**
* **02_First_Service**
* **03_Service_Discovery**
* **04_Hateoas**
* **05_Eureka_And_Ribbon**

Checkout a sub branch
```
git checkout -b [BRANCH_ID]_[BRANCH_NAME] origin/[BRANCH_NAME], e.g. git checkout -b 02_First_Service origin/02_First_Service
```

## Demonstrated Principles

The application will provide a set of common good practices for Microservice development. To get accustomed with general principles of distributed application development a good source of reading as a starter is [The Twelve-Factor App](https://12factor.net/). You'll see a lot of those principles already built-in in Spring Cloud.

### Simple Spring Boot Microservice

**01_Initial_Boot_Setup** and **02_First_Service** demonstrate how to get started with Spring Cloud Microservice Development. In a few simple steps we will see
how easy it is to create a production ready Microservice in almost no time.

### Service Registry and Discovery

**03_Service_Discovery** shows how to set up a service registry in Spring Boot and how to let services communicate with each other through this registry.

### Hateoas Support

**04_Hateoas** makes a short excursion to the field of RESTful API design and how to create [mature](https://martinfowler.com/articles/richardsonMaturityModel.html)
REST APIs.

### Client-side load-balancing with Eureka and Ribbon

**05_Eureka_And_Ribbon** goes one step further into a more advanced topic - client-side load-balancing - which we can use almost out-of-the-box with Spring Cloud's
Ribbon starter project.

## Detail Description

See [Detail Description](DETAIL-DESCRIPTION.md) for an in-depth explanation of the current features of this demo application.

## How-to run the app

See [How-to run](HOW-TO-RUN.md) for further details.

## References

[Spring Boot 2](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2.0-Release-Notes)
[Spring Cloud](https://spring.io/blog/2018/06/19/spring-cloud-finchley-release-is-available)
