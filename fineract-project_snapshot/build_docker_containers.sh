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

fineractProjectFolder="fineract-project"

#applicationYmlFolder="service/src/main/resources"
#applicationYmlFile="application.yml"
#applicationYamlFile="application.yaml"
# the version must match the configuration of the name in the bootstrap.yml of the microservice
#applicationYmlVersion="v1"

# create config directory (if it does not exist yet)
#mkdir -p config

# move into fineract-project folder
cd $fineractProjectFolder

# define the microservices
microservices=("identity" "office" "customer" "accounting" "portfolio" "provisioner")

# loop over defined microservices
for ms in "${microservices[@]}"
do
    if [ "$ms" == "provisioner" ] ; then
        port=9090
    fi

    # go into microservice directory
    cd $ms

    # create docker-file for the microservice
    echo ::::: Create Dockerfile $ms
    echo 'FROM openjdk:8-jre-alpine' > Dockerfile
    echo 'VOLUME /tmp' >> Dockerfile
    echo 'ADD service/build/libs/service-0.1.0-BUILD-SNAPSHOT-boot.jar app.jar' >> Dockerfile
    echo "RUN /bin/sh -c 'touch /app.jar\'" >> Dockerfile
    echo 'ENV JAVA_OPTS=""' >> Dockerfile
    echo 'EXPOSE '${port} >> Dockerfile
    echo 'ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]' >> Dockerfile
    echo ::::: Building docker container $ms

    # build docker-container for microservice
    docker build -t $dockerTagPrefix/$ms:$dockerTagVersion . 

    # copy the application.yml file of the microservice to the config directory
    #applicationYmlConfigFile=$ms-$applicationYmlVersion-default.yml
    #{ # try
    #    cp $applicationYmlFolder/$applicationYmlFile ../config/$applicationYmlConfigFile
    #} || { # catch
    #    cp $applicationYmlFolder/$applicationYamlFile ../config/$applicationYmlConfigFile
    #}
    cd ..
done

## fims web-app not within docker container in current setup, run the fims-web-app from console by hand (e.g. "npm run dev" in the fims-web-app folder) 
#echo ::::: Build Fims-Web-App
#cd fims-web-app
#npm run build
#echo 'FROM nginx:1.11-alpine' > Dockerfile
#echo 'COPY dist /usr/share/nginx/html' >> Dockerfile
#docker build -t $dockerTagPrefix/fims-web-app:$dockerTagVersion .
#cd ..
