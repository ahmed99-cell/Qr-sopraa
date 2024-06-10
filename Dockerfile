FROM openjdk:17-jdk

WORKDIR /app

COPY target/spring-boot-security-postgresql-0.0.1-SNAPSHOT.jar /app/spring-boot-security-postgresql.jar

EXPOSE 8081

CMD ["java", "-jar", "spring-boot-security-postgresql.jar"]