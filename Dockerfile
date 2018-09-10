FROM base-jdk11:latest AS builder

MAINTAINER lesh<alexey.lesh@gmail.com>

COPY ./src /builddir
COPY ./gradlew /builddir
COPY ./gradle /builddir/gradle

WORKDIR /builddir
RUN ./gradlew build

WORKDIR /app

COPY build/libs/statistics-svc-0.1.0.jar .

RUN jlink --module-path statistics-svc-0.1.0.jar:$JAVA_HOME/jmods \
#        --add-modules com.jdriven.java9runtime.frontend \
#        --launcher run=com.jdriven.java9runtime.frontend/com.jdriven.java9runtime.frontend.FrontendApplication \
        --add-modules company.challenge \
        --launcher run=company.challenge/company.challenge.App \
        --output dist \
        --compress 2 \
        --strip-debug \
        --no-header-files \
        --no-man-pages

#https://blog.jdriven.com/2017/11/modular-java-9-runtime-docker-alpine/

#jlink --module-path statistics-svc-0.1.0.jar:$JAVA_HOME/jmods --add-modules company.challenge --launcher run=company.challenge/company.challenge.App --output dist --compress 2 --strip-debug --no-header-files --no-man-pages