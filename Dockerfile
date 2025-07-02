FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Kopiera filerna först
COPY . .

# ✅ Ge körbar rättighet till mvnw
RUN chmod +x mvnw

# ✅ Förbered dependencies (valfritt men bra)
RUN ./mvnw dependency:go-offline

# ✅ Bygg appen
RUN ./mvnw clean package -DskipTests

EXPOSE 8080

CMD ["java", "-jar", "target/demo-0.0.1-SNAPSHOT.jar"]
