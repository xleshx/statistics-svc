#!/bin/sh

trap 'echo "SIGTERM received for process $PID. Terminating." ; kill -TERM $PID' TERM INT

version=`java -version 2>&1 | head -n 1 | cut -d '"' -f 2`

echo "Java version is $version"

JVM_OPTIONS="\
 -XX:+CrashOnOutOfMemoryError\
 -XX:+HeapDumpOnOutOfMemoryError\
 -XX:-OmitStackTraceInFastThrow\
 -XX:HeapDumpPath=/srv/app/logs/heapd.hprof"

APP_NAME=statistics-svc
JAR=$APP_NAME.jar
SPRING_PROFILES=prod

java \
  -Dserver.port=8080\
  -Dmanagement.contextPath=/management\
  -Dspring.profiles.active=${SPRING_PROFILES}\
  -Dlogging.application.name=${APP_NAME}\
  -Dfile.encoding=UTF-8\
  $JVM_OPTIONS\
  -jar /srv/app/$JAR &
PID=$!
echo "Java process $PID started"
wait $PID
echo "Shutting down java process ${PID}"
wait $PID
echo "Java process ${PID} shut down"
