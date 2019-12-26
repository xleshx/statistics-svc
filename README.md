### What is this repository for? ###
Simple service built on Spring stack which returns statistics calculated for the last 60 seconds

### Run ###
Locally

export JAVA_HOME={path to java 13 sdk}
./gradlew clean build && java -jar build/libs/statistics-svc-0.1.0.jar

Docker

docker build -t local/statistics-srv:v1 .
docker run -p 8080:8080 local/statistics-srv:v1 

### Check the code ###

Project uses Lombok.
In order to comfortable look through the code use IDE plugins  

### TODO ###
- Fix NPE on first call to statistics 

