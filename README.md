# BankingInTheCloud-Fineract

This repository provides a runnable snapshot of the [fineract project](https://github.com/mifosio/). 

The master branch holds the latest stable version. New snapshots are created in branches and merged to the master once tested. 

## Project information

### Core Services

We call the services that all the other services depend on "core services":

* ***config*** (spring-boot config server): the configuration server used to configure the services without having to deploy them when the configuration changes
* ***discovery*** (eureka): the service registry / service discovery - through this service the microservices find each other
* ***cassandra***: A no-sql database used to store the events
* ***activemq***: The event bus that handles the event sourcing (events-history stored in cassandra)
* ***mariadb*** (mysql): The database where all the actual relational data is stored


### Functional Services

We call the services that hold the functional parts of the application "functional services":

* ***provisioner***
* ***identity***
* ***office***
* ***customer***
* ***accounting***
* ***portfolio***

## Running the project

### Building the microservices

### Building the containers

### Running the microservices

#### Running the core-services

#### Running the fims-web-app

#### Running the functional services

### Initializing the application by sending requests through postman

#### Loading and sending requests

#### First sign-in

Requirement: all the requests from postman were successfully sent.

The following user-profiles are available in fims-web-app after above setup was completed successfully:

```
tenant: senacor-bitc
user: antony
password: test
```

```
tenant: senacor-bitc
user: fims
password: p@s$w0r&
```

Note that user antony does not have any roles set  up, so if you log in as antony you cannot really do anything. 
User fims is created in the the last two requests (user creation and role assignment) in the postman request-list. This user has admin rights and ist able to manage offies, accounts, customers (...).

To sign in the microservices (core and functional services) have to be started, as well as the fims-web-app. Navigate to localhost:4200 in your browser and enter the credentials of the user you want to sign in with. 


## Extending the project

### Remote debugging

In order to remote-debug the running application (running in docker-containers) you have to expose the debug-port when starting up the web-server (Jetty).
The [Jetty-documentation](http://www.eclipse.org/jetty/documentation/9.3.x/enable-remote-debugging.html) states that you have to add the following Java-VM startup parameters:
```
-Xdebug
``` 

and 
```
-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8090
```

Note that ```-Xdebug``` is only necessary if you sun an old Java version. From 1.6 up you should not need it any more (but you can still add it, it will be ignored).


For the functional service we already set the remote debugging as enabled through the generated docker files. You can check in the Dockerfile each functional service folder.


### Project Setup in IntelliJ IDEA

1. Import one of the functional microservices
2. Add all other microservices using File >> New >> Module from existing sources...

### 
