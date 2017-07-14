#!/bin/bash

# move to fineract project folder
cd ..
docker-compose up config discovery cassandra mariadb activemq
