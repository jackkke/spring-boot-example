# syntax=docker/dockerfile:1

FROM openjdk:11.0.15-jre-slim

MAINTAINER jackkke Docker springboot "hangao1204@hotmail.com"

# 使用非 root 用户启动
RUN addgroup zion
RUN adduser jackkke
RUN adduser jackkke zion
USER jackkke:zion

# Spring Boot Layer Index
ARG EXTRACTED=target/extracted
COPY ${EXTRACTED}/dependencies/ ./
COPY ${EXTRACTED}/spring-boot-loader/ ./
COPY ${EXTRACTED}/snapshot-dependencies/ ./
COPY ${EXTRACTED}/application/ ./

EXPOSE 9716

ENTRYPOINT ["java","org.springframework.boot.loader.JarLauncher"]