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
ARG DEPENDENCY=target/dependency
COPY ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY ${DEPENDENCY}/META-INF /app/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes /app

EXPOSE 9716

ENTRYPOINT ["java","-cp","/app:/app/lib/*","top.jackkke.DockerBuildApplication"]