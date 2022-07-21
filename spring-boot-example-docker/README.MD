# 镜像选择
jdk8 -> openjdk:8u212-jre-alpine(推荐) openjdk:8u212-alpine 版本号为 8u212
jdk11 -> openjdk:11.0.15-jre-slim(推荐) 版本号为 11.0.15
         jib使用上面镜像打包会失败
                  可以使用官方推荐 eclipse-temurin:{8\11\17}-jre 指定自己的jre版本
                  或 adoptopenjdk/openjdk11:jre-11.0.15_10-alpine(版本号为jdk-11.0.15+10) 代替
jdk17 -> openjdk:17-alpine(推荐) 版本号为 17-ea+14

# 01 简单打包
# 02 分离 jar 打包
# 03 插件com.spotify:dockerfile-maven-plugin打包
# 04 插件jib
# 05 分层 打包 Docker 映像层更加高效