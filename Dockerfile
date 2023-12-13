FROM openjdk:17-jdk
EXPOSE 8080:8080
RUN mkdir /app
COPY build/libs/ktor-rma-all.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
