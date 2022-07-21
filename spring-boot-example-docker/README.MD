# 镜像选择
jdk8 -> openjdk:8u212-jre-alpine(推荐) openjdk:8u212-alpine 版本号为 8u212
jdk11 -> openjdk:11.0.15-jre-slim(推荐) 版本号为 11.0.15
         jib使用上面镜像打包会失败
                  可以使用官方推荐 eclipse-temurin:{8\11\17}-jre 指定自己的jre版本
                  或 adoptopenjdk/openjdk11:jre-11.0.15_10-alpine(版本号为jdk-11.0.15+10) 代替
jdk17 -> openjdk:17-alpine(推荐) 版本号为 17-ea+14

# 参考资料
    https://spring.io/guides/gs/spring-boot-docker/
    https://spring.io/guides/topicals/spring-boot-docker/

# 01 简单打包
# 02 分离 jar 打包
# 03 插件com.spotify:dockerfile-maven-plugin打包
    https://github.com/spotify/dockerfile-maven
# 04 插件jib
    https://github.com/GoogleContainerTools/jib/tree/master/jib-maven-plugin
# 05 分层 打包 Docker 映像层更加高效
# 06 使用 springboot 官方插件 spring-boot-maven-plugin
    https://docs.spring.io/spring-boot/docs/current/maven-plugin/reference/htmlsingle/#goals-build-image
    mvn spring-boot:build-image
    打包过程依赖的镜像较大，且后续从Github下载jdk大概率需要翻墙
    暂时丢弃 -> 待后续看是否可以优化