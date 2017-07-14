# Creating a fineract snapshot

1. Checkout the fineract project using script ```fineract-project.checkout.sh```. this will create a folder structure for you. Note that you might have to update the script first (add new github repos that were added to fineract).
2. Copy the necessary scripts (from the current scapshot version) to the root folder ```fineract-project```.
3. Copy the config folder to the new 
4. Adapt the existing scripts and config files so they fit the current version of fineract. 
5. Get the project to run (build all containers, start all containers, start fims-web-app ...)
6. Once everything works as expected you can run the cleanup script ```fineract-project-snapshot-cleanup.sh```.
7. Create a new branch with pattern ```fineract-snapshot-YYYYMMDD```.
8. Overwrite all files and folders in fineract-project_snapshot folder with those of the new snapshot.
9. Commit the changes on your branch, create a pull-request. 
10. After the snapshot in the branch was successfully tested on another computer the pull-request can be merged, so the master becomes the new snapshot.
