FROM openjdk:17
MAINTAINER tranvanloc23.1.2003@gmail.com
EXPOSE 7070
COPY chatapp/target/spring-chatapp-v1.0.0.jar spring-chatapp-v1.0.0.jar
ENTRYPOINT ["java","-jar","/spring-chatapp-v1.0.0.jar"]