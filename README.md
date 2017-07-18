# BankingInTheCloud-Fineract

This repository provides a runnable snapshot of the [fineract project](https://github.com/mifosio/). This repository was created to ensure the project setup during the BankingInTheCloud workshop.

The master branch holds the latest stable version. New snapshots are created in branches and merged to the master once tested. 

## Project information

The fineract project is based on a microservices architecture. There are several independent services that communicate with each other over HTTP (REST services). In order to make it easier to discuss certain setup issues we classified the services into two main categories.

### Core Services

We call the services that all the other services depend on "core services":

* **config** (spring-boot config server): the configuration server used to configure the services without having to deploy them when the configuration changes
* **discovery** (eureka): the service registry / service discovery - through this service the microservices find each other
* **cassandra**: A no-sql database used to store the events
* **activemq**: The event bus that handles the event sourcing (events-history stored in cassandra)
* **mariadb** (mysql): The database where all the actual relational data is stored


### Functional Services

We call the services that hold the functional parts of the application "functional services":

* **provisioner**
* **identity**
* **office**
* **customer**
* **accounting**
* **portfolio**

## Running the project locally

This section describes how you can build and run the microservices on your machine, locally.

The project files and build scripts are located in the [fineract-project_snapshot folder](https://github.com/senacor/BankingInTheCloud-Fineract/tree/master/fineract-project_snapshot).

This folder contains all the files necessary to build the microservices and containers and run them. The build scripts mentioned in the following sub-section are located in that folder as well.

### Requirements

The following tools have to be available:

* Java 1.8.x
* nodeJs v6.x + npm
* docker
* docker-compose
* Postman (Google Chrome extension)

To make the setup easier we provide a Vagrant machine that includes all the tools mentioned above. 

For details on the setup please refer to the [BankingInTheCloud-WorkshopSetup repository](https://github.com/senacor/BankingInTheCloud-WorkshopSetup).

### Publishing the microservices

All microservices are gradle projects, so in order to publish them (to maven local) they have to be built and published (jar copied to maven local).
This is done by running the gradle task:
```
./gradlew publishToMavenLocal
```

Note that the order in which to build the microservices is really important as they depend on each other. If a microservice depends on another it expects the built microsoervice (the dependency) in the maven local directory structure at build time. 

We provide a build-script that runs the complete build in the right order:
```
build_microservices.sh
```

You can just run this build-script and all the microservices will be published. Then you can go on with building the docker-containers for the microservices.
Note that the order is defined by the dependencies of the microservices on each other - if the dependencies change the build order might have to be adapted!


### Building the containers
In order to build the docker-containers the packed microservices have to be published to maven local (as jar files). This is done in the build-step (see above).

We distinguish the docker containers for the core services and the docker containers for the docker containers for the functional services. The docker containers for the core-services do not have to built. They use standard images that are defined in the docker compose file (see next section).

For the functional services we generate a Dockerfile that tells docker:

1. which image to use,
2. how to start the application,
3. at which port the application will be available.

Once the Dockerfile was generated we run ```docker build -t TAG_OF_CONTAINER``` to build the container. 
You can run the build-script that generates the Dockerfiles by running the container build-script:
```
build_docker_containers.sh
```

Once all containers were built we can start up the microservices using docker-compose. 

### Running the microservices

For starting the containers we use docker-compose. The run-configuration for the containers is defined by file:
```
docker-compose.yml
```

Each service has an entry in the yaml file that defines how it will be started. For the core services there are images defined that will be used. For the function services the working directory and context - where the generated Dockerfiles can be found - is defined.

The core services containers have to be started before starting the function microservice containers. This is because the configuration server, discovery, event bus and the databases have to be available when the microservices start so they can utilize them upon startup (especially the configuration server is necessary, otherwise the microservices cannot be configured correctly). Thus we start the core services first.

#### Running the core-services

You can just start the core services by running ```docker-compose up```:
```
docker-compose up config discovery cassandra mariadb activemq
```

If you want to see the command line output of each service in a separate terminal you can also start each service in a separate terminal. 

You can use this script to run the start command: 
```
start_core-microservices.sh
```

If you are using the vagrant VM that we provide for the setup you can use the following script to start the core services in different terminals (separate terminal for each container): 
```
vm-scripts/start_core-microservices_separete_terminals.sh
```

There will be quite a lot of console output upon statup. The startup should be finished when you see this message:
```
Started EurekaApplication in xx.xxx seconds (JVM running for xx.xxx)
```

Note that it can happen that eureka is not the last service to finish the startup. In case one of the other services finishes last there will be a different service name printed last. 

You can scroll up to check the command line output of all the services.


#### Running the functional services

Once the core services are started up you can start the functional microservices by running the following command:
```
docker-compose up provisioner customer accounting identity office portfolio
```

You can use the start-script to run the command:
```
start_functional-microservices.sh
```

If you are using the vagrant VM that we provide for the setup you can use the following script to start the core services in different terminals (separate terminal for each container): 
```
vm-scripts/start_functional-microservices_separete_terminals.sh
```

You should retrieve some ```Jetty started on port(s)``` message from every service eventually; it can look like this:
```
identity       | 18:38:21.708 [main] INFO  o.s.b.c.e.j.JettyEmbeddedServletContainer - Jetty started on port(s) 8081 (http/1.1)
```

#### Running the fims-web-app

Navigate into the [fims-web-app folder](https://github.com/senacor/BankingInTheCloud-Fineract/tree/master/fineract-project_snapshot/fineract-project/fims-web-app):
```
fineract-project/fims-web-app
```

Run:
```
npm run build
```

Then run:
```
npm run dev
```

Once started the fims-web-app will be available in your browser at ```localhost:4200```.

Hint: Starting the fims-web-app can take some time. Being stuck at ```92% (...)``` is normal. Just give it some time.

Once the fims-web-app was started successfully the last output line should be:
```
webpack: Compiled successfully.
```

If the fims-web-app could not be started successfully you will see nodeJs errors as command line output.

### Initializing the application by sending requests through postman

Once all services (and the fims-web-app) are started you will have to create some initial data (user to log in, ...).  We do this by using Postman. Postman is a tool to send HTTP requests to a defined server. This is very useful when communicating with REST services. One can comfortably manage the header, body and response there. You can define variables within Postman that can be set by the response of requests received from a server after sending a request there. Like this one can define request collections where requests build on the results of other requests.

#### Loading and sending requests

We provide a postman-request-collection as well as a postman-environment that defines variables that are used to hold values received in responses. Both files are located in the 
[postman-initial-requests folder](https://github.com/senacor/BankingInTheCloud-Fineract/tree/master/fineract-project_snapshot/postman-initial-requests):
```
postman-initial-requests/Fineract-Initial-Setup-Environment.postman_environment.json
postman-initial-requests/Fineract-Initial-Requests.postman_collection.json
```

Basically you have to do the following:

1. Start Postman and load both files into Postman by clicking ```Import``` and then selecting the file.
2. You will see the collection "Fineract-Initial-Requests" in the left sidebar.
3. Open the collection by clicking on it.
4. Select the environment "Fineract-Initial-Setup-Environment" in the environment drop-down (top right corner in Postman).
5. Execute the requests one by one by selecting them in the collection and then pressing "Send".

Note: If you receive the error ```Failed to import data: Could not import: TypeError: Cannot read property 'id' of null``` upon importing the files you can ignore it.

The first request will retrieve a token. For this request you basically only need the ```provisioner``` and the ```identity``` service running (until request 04.1 you don't need the other services to be started). The response should look like this (obviously with a different token...):
```
{
    "token": "Bearer eyJhbGciOiJSUzUxMiJ9.eyJhdWQiOiJwcm92aXNpb25lci12MSIsInN1YiI6IndlcGVtbmVmcmV0IiwiL21pZm9zLmlvL3NpZ25hdHVyZVRpbWVzdGFtcCI6IjIwMTctMDQtMThUMDlfNDRfMjIiLCIvbWlmb3MuaW8vdG9rZW5Db250ZW50IjoiUk9MRV9BRE1JTiIsImlzcyI6InN5c3RlbSIsImlhdCI6MTUwMDA1NjgxNywiZXhwIjoxNTAwNDE2ODE3fQ.OfxTUTStJbKQc4rAPW5PLIQYNjCG_uqcNPR4up6pIQBWLDxkgEiU9EF1WrB5NQdzXBJIHqjDFQpaVywm5DersIh4LxPGD3MZj3TqZK5_LUcZvBDTa4Xgb41e3xXkWB4TkN6KqfmiK12Ngjrrj7qZGBdtypDmFmZwKQRZIOL6T3QbI7LpbPGpeWjpWZirFgtcn5B1Z_h3r9rirCzecUdVjlaplQufxDuVFJS0R3N67pyuGQENvCAC716ID5KbokTQtITXfjnCztFuQBbtCPcYLIzxsKv_-E5k6Gd0pv01OC0XpY3NSgfAolVVgvSXKoRnL3NwAMP2yuzX6i8hR_q82Q",
    "accessTokenExpiration": "2017-07-18T22:26:57.784"
}
```

If you don't get a token there is something wrong with your setup. The token is necessary for authentication in other requests thus be sure that this steps works before you go on.


#### What if you don't retrieve a token

1. Check if the microservices ```provisioner``` and ```identity``` are running correctly. It can help to start only those two services in different terminals and check the command line output.
2. Check if all core services are running correctly. Check the command line output upon startup.

#### Resetting the databases if something goes wrong
Important: Be sure to execute the requests in the right order! If you execute the requests that gives you the initial password (request "03.2 Create Identity Service for Tenant") twice you will not be able to retrieve the initial password again (due to the implementation of the identity service). If that happens - the variable antonyUserPassword is empty (undefined) - then you have to remove the relevant entries from the databases. Since this is rather complicated to do by hand we recommend that you just reset the databases and start again.

The easiest way to reset the databases is to remove the containers ```cassandra``` and ```mariadb```. 
You can use the following command to retrieve the list of containers including their IDs (first column):
```
docker ps -a
```  

Once you have the ID of the containers to be removed you can use this command to remove the container:
```
docker rm CONTAINER_ID
```

#### First sign-in in fims-web-app

Requirement: all the requests from postman were successfully sent.

Navigate to localhost:4200 in your browser and enter the credentials of the user you want to sign in with.

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

Note that user antony does not have any roles set  up, so if you log in as ```antony``` you cannot really do anything. 
User ```fims``` is created in the the last two requests (user creation and role assignment) in the postman request-list. This user has admin rights and is able to manage offices, accounts, customers (...).

Login is possible after the microservices (core and functional services), as well as the fims-web-app, are all started.  






## Running the project in the Cloud

Note: this part of the documentation is to be filled in!

### Moving config and discovery to the cloud

As a first step we want to save some resources on our local machine. Running all the services locally is quite resource-intense, so let's move some of them to the cloud.

As a start we move the config server and the discovery (Eureka) to the cloud, because those services can be easily extracted and they can be shared between users (more on that later).

(...) 


### Moving all the services to the cloud

Careful: This can get expensive ;)



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

Note that ```-Xdebug``` is only necessary if you run an old Java version. From 1.6 up you should not need it any more (but you can still add it, it will be ignored).

For the functional service we already set the remote debugging as enabled through the generated docker files. You can check in the Dockerfile each functional service folder.


### Project Setup in IntelliJ IDEA

1. Import one of the functional microservices (does not really matter which one)
2. Add all other microservices using File >> New >> Module from existing sources...

### Connect to the databases with dBeaver

You can use the tool of your choice to connect to the databases - we will explain the setup with dBeaver here.

Download and install the [Enterprise Edition of dBeaver](http://dbeaver.jkiss.org/download/enterprise/). Note that its important that you download the enterprise edition - the community edition does not include the drivers for cassandra out of the box. The enterprise edition is for free as well, but not open source.

#### Connect to cassandra

1. Open dBeaver Enterprise Edition
2. Select "New Connection" (on first startup this will pop up automatically)
3. Select "Cassandra CQL" from the driver list >> Next
4. Check port is set to 9042 (should be by default) >> Next >> Next
5. Finish

#### Connect ot mariadb

In order to connect to mariadb you need the database, user and password defined for the database. You can find this in the provisioner configuration yaml file in the [config folder](https://github.com/senacor/BankingInTheCloud-Fineract/tree/master/fineract-project_snapshot/config).

User and password should looks like this:
```
database: seshat
user: root
password: mysql
```

1. Open dBeaver Enterprise Edition
2. Select "New Connection" (on first startup this will pop up automatically)
3. Select MySQL >> Next
4. Enter database, user and password >> Next >> Next
5. Finish