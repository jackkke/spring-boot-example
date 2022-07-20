# syntax=docker/dockerfile:1

FROM openjdk:11.0.15-jre-slim

MAINTAINER jackkke Docker springboot "hangao1204@hotmail.com"

# 使用非 root 用户启动
RUN addgroup zion
RUN adduser jackkke
RUN adduser jackkke zion
USER jackkke:zion

WORKDIR /app

# 直接使用打包好的jar来启动
ARG JAR_FILE=spring-boot-example-docker01-2.6.3.jar
COPY target/${JAR_FILE} /app/jackkke.jar

EXPOSE 9716

ENTRYPOINT ["java", "-jar", "/app/jackkke.jar"]