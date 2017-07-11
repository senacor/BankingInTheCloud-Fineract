#!/bin/bash

# this script can be used when creating a new snapshot of the fineract project 
# (moving the multi-repo structure to one single repo including all startup scripts

# remove all .github directories
find . -name .github -type d -print0|xargs -0 rm -rf

# remove all .git directories
find . -name .git -type d -print0|xargs -0 rm -rf

# remove all Dockerfiles (will not be committed since they are generated)
find . -name Dockerfile -type f -print0|xargs -0 rm -r