### What is this repository for? ###
Simple service built on Spring stack that returns statistics for calculated for last 60 secords of data.

### Run ###

./gradlew clean build && java -jar build/libs/statistics-svc-0.1.0.jar.jar


### Check the code ###

In order to comfortable look through the code lombok support for IDE is recommended 

### Contact ###

alexey.lesh@gmail.com


INSERT INTO TRANSACTION VALUES(3, 1000,200000)

SELECT * from transaction

SELECT sum(t.amount), avg(t.amount), max(t.amount), min(t.amount), count(t.amount), t.TIMESTAMP as ts
FROM TRANSACTION t
GROUP BY  t.TIMESTAMP 
HAVING t.timestamp > ts