# dropwizard-profile-viewer
Demonstration of dropwizard framework with an integration test

###Instructions how to run the assignment:
You can setup the database in 2 diferrent ways:
Run 
```sh  
“java –jar profile-viewer-1.0-SNAPSHOT.jar db migrate app-configuration.yml"
```
Or you can uncoment the *hibernate.hbm2ddl.auto* property in app-configuration.yml and leave it to hibernate

###Run Profile Viewer
```sh  
“java –jar profile-viewer-1.0-SNAPSHOT.jar server app-configuration.yml"
```
###Play with it
There is also a postman file *profile_viewer.json.postman_collection* which includes some test rest calls. The provided file could be imported into http://www.getpostman.com/.


