#!/bin/bash

cd fineract-project
# remember fineract-project as working directory
workingDir=$(pwd)

# define the paths to all microservices (including all the core services and all the tools)
# note that the order is of vital importance!
# if there is an error during the build you may have to change the order (if a service is dependent on another then it will expect it in maven-local when building, thus it has to be built before)
microservices-publish=("core/lang" "core/api" "core/async" "core/cassandra" "core/mariadb" "core/data-jpa" "core/command" "core/test" 
               "tools/javamoney-lib" "tools/crypto" 
               "anubis" 
               "identity" 
               "permitted-feign-client" 
               "provisioner" 
               "rhythm" 
               "template" 
               "office" 
               "customer"
               "group"
               "accounting"
               "portfolio"
               "integration-tests/service-starter" "integration-tests/demo-server"
               )

microservices-build-only=("integration-tests/test-provisioner-identity-organization" "integration-tests/test-accounting-portfolio")

# build and publish to maven local
for ms in "${microservices-publish[@]}"
do
    # navigate to folder of service to build
    cd $ms

    # special handling for javamoney-lib
    if [ "$ms" == "tools/javamoney-lib" ]
      then
        mvn install -Dmaven.test.skip=true
      else

        # publish the service to maven-local
        chmod +x gradlew
        ./gradlew publishToMavenLocal

    fi

    # navigate back to working directory
    cd $workingDir
done

# build only services
for ms in "${microservices-publish[@]}"
do
    # navigate to folder of service to build
    cd $ms

    chmod +x gradlew
    ./gradlew build

    # navigate back to working directory
    cd $workingDir
done

# fims-web-app
cd fims-web-app
npm i


