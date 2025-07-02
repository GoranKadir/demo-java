# Dockerfile
FROM eclipse-temurin:17-jdk-alpine

# Skapa appkatalog
WORKDIR /app

# Kopiera pom och källkod
COPY pom.xml ./
COPY mvnw ./
COPY .mvn .mvn
COPY src ./src

# Bygg jar-filen
RUN ./mvnw clean package -DskipTests

# Starta appen
CMD ["java", "-jar", "target/demo-0.0.1-SNAPSHOT.jar"]
