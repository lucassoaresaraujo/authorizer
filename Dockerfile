FROM maven:3.9.4-eclipse-temurin-21 AS BUILD_IMAGE

WORKDIR /app

COPY . /app

RUN mvn clean package -DskipTests --no-transfer-progress \
    && apt-get clean \
    && rm -rf /root/.m2

FROM eclipse-temurin:21-jre-jammy

COPY --from=BUILD_IMAGE /app/target/*.jar /opt/app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-Xms2048M", "-Xmx2048M", "-jar", "/opt/app.jar"]