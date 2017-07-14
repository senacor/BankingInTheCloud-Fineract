#!/bin/bash

# move to fineract-project folder
cd ..

lxterminal --command "bash -c 'docker-compose up config && bash'" --title="config"
lxterminal --command "bash -c 'docker-compose up discovery && bash'" --title="discovery"
lxterminal --command "bash -c 'docker-compose up cassandra && bash'" --title="cassandra"
lxterminal --command "bash -c 'docker-compose up mariadb && bash'" --title="mariadb"
lxterminal --command "bash -c 'docker-compose up activemq && bash'" --title="activemq"
