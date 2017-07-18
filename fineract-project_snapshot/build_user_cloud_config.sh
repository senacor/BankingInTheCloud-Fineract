#!/bin/bash

## generates a docker-compose.yml-file with correct settings, to work with cloud config server

userName=$1
appStage=$2
microservices=("identity" "office" "customer" "accounting" "portfolio" "provisioner")

if [ $userName == "" ]
  then
    userName="olaf"
fi
if [ $appStage == "" ]
  then
    appStage="dev"
fi

sed 's/<user>/'$userName'/g; s/<stage>/'$appStage'/g' docker-compose-skeleton.yml > docker-compose.yml

for ms in "${microservices[@]}"
do
  cp "./config/"$ms"-v1-default.yml" "./config/"$ms"_"$userName"-v1-"$userName"_"$appStage".yml"
done
