FROM adoptopenjdk/openjdk13-openj9:jdk-13.0.1_9_openj9-0.17.0-alpine-slim AS builder

COPY ./src /builddir/src
COPY ./gradlew /builddir
COPY ./gradle /builddir/gradle
COPY ./*.gradle /builddir/

WORKDIR /builddir
RUN ./gradlew build

FROM adoptopenjdk/openjdk13-openj9:jdk-13.0.1_9_openj9-0.17.0-alpine-slim
MAINTAINER lesh<alexey.lesh@gmail.com>

COPY --from=builder builddir/build/libs/statistics-svc-0.1.0.jar /srv/app/

WORKDIR /srv/app

COPY run.sh .
RUN chmod u+x run.sh

CMD ["/srv/app/run.sh"]
