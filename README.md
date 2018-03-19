### What is this repository for? ###
Simple service built on Spring stack that returns statistics for calculated for last 60 secords of data.

### Run ###

./gradlew clean build && java -jar build/libs/statistics-svc-0.1.0.jar.jar


### Check the code ###

In order to comfortable look through the code lombok support for IDE is recommended 

### Contact ###

alexey.lesh@gmail.com


curl -X POST -d '{"amount":1000,"timestamp":12341234123}' localhost:8080/transactions
curl -d '{"amount":1000,"timestamp":12341234123}' -H "Content-Type: application/json" -X POST http://localhost:8080/transactions
0144 -> 100
0005 -> 200 

1115 -> 100
1455 -> 100
1788 -> 100

2115 -> 200
2455 -> 200
2788 -> 200
