#!/bin/bash

## call script like this for first time: ./build_all_docker_containers.sh 0.1.0 v1

dockerTagPrefix=$1
dockerTagVersion=$2
# set the variables to default "0.1.0" and "v1" if they were not set by user
if [$dockerTagPrefix == ""]
  then
    dockerTagPrefix="0.1.0"
fi
if [$dockerTagVersion == ""]
  then
    dockerTagVersion="v1"
fi

port=8081
portCounter=1
debugPortOffset=1000

fineractProjectFolder="fineract-project"

# move into fineract-project folder
cd $fineractProjectFolder

# define the microservices
microservices=("identity" "office" "customer" "accounting" "portfolio" "provisioner")

# loop over defined microservices
for ms in "${microservices[@]}"
do
    if [ "$ms" == "provisioner" ] ; then
        port=9090
    else
        port=$(($port + $portCounter))
    fi

    debugPort=$(($port - $debugPortOffset))

    # go into microservice directory
    cd $ms

    # create docker-file for the microservice
    echo ::::: Create Dockerfile $ms with port $port and debug-port $debugPort
    echo 'FROM openjdk:8-jre-alpine' > Dockerfile
    echo 'VOLUME /tmp' >> Dockerfile
    echo 'ADD service/build/libs/service-0.1.0-BUILD-SNAPSHOT-boot.jar app.jar' >> Dockerfile
    echo "RUN /bin/sh -c 'touch /app.jar\'" >> Dockerfile
    echo 'ENV JAVA_OPTS=""' >> Dockerfile
    echo 'EXPOSE '${port} >> Dockerfile
    echo 'ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom", "-Xdebug", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address='${debugPort}'","-jar","/app.jar"]' >> Dockerfile
    echo ::::: Building docker container $ms

    # build docker-container for microservice
    docker build -t $dockerTagPrefix/$ms:$dockerTagVersion . 

    cd ..
done

