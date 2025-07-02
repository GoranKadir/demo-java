# Dockerfile
FROM eclipse-temurin:21-jdk-alpine

# Ange arbetsmapp i containern
WORKDIR /app

# Kopiera pom.xml och wrapper-filer först (för att cacha dependencies)
COPY pom.xml mvnw ./
COPY .mvn .mvn

# Ladda ner dependencies
RUN ./mvnw dependency:go-offline

# Kopiera resten av koden
COPY src ./src

# Bygg applikationen
RUN ./mvnw clean package -DskipTests

# Starta jar-filen
CMD ["java", "-jar", "target/demo-0.0.1-SNAPSHOT.jar"]
