#!/bin/bash

# move to fineract-project folder
cd ..

lxterminal --command "bash -c 'docker-compose up provisioner && bash'" --title="provisioner"

lxterminal --command "bash -c 'docker-compose up customer && bash'" --title="customer"
lxterminal --command "bash -c 'docker-compose up accounting && bash'" --title="accounting"
lxterminal --command "bash -c 'docker-compose up identity && bash'" --title="identity"
lxterminal --command "bash -c 'docker-compose up office && bash'" --title="office"
lxterminal --command "bash -c 'docker-compose up portfolio && bash'" --title="portfolio"
