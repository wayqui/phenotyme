FROM --platform=linux/amd64 maven:3.8.6-jdk-8-slim

RUN mkdir /var/maven/ && chmod -R 777 /var/maven  && umask 0777 /var/maven
ENV MAVEN_CONFIG /var/maven/
ENV MAVEN_CONFIG /var/maven/.m2

RUN mkdir /app
ADD . /app

WORKDIR /app

RUN mvn -Duser.home=/var/maven -Dmaven.repo.local=/var/maven/.m2 -f pom.xml install -DskipTests

EXPOSE 8080

CMD ["java", "-Xmx1024m", "-Xms1024m", "-jar", "target/core-nlp-api-0.0.1-SNAPSHOT.jar"]
