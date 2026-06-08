# Builds the Spring Boot backend with Java 17, then runs the packaged jar.
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /workspace

# Use an Aliyun Maven mirror to make dependency downloads smoother on mainland servers.
COPY docker-settings.xml /root/.m2/settings.xml
COPY pom.xml ./
RUN mvn -B -ntp dependency:go-offline

COPY src ./src
RUN mvn -B -ntp clean package -DskipTests

FROM eclipse-temurin:17-jre

WORKDIR /app
ENV TZ=Asia/Shanghai

COPY --from=build /workspace/target/backend-0.0.1-SNAPSHOT.jar /app/backend.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/backend.jar"]
