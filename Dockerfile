FROM maven:3.9-openjdk-21-slim AS build

WORKDIR /app

# Copiar archivos de Maven
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Descargar dependencias
RUN mvn dependency:go-offline -B

# Copiar código fuente
COPY src src

# Construir la aplicación
RUN mvn clean package -DskipTests

# Etapa de ejecución
FROM openjdk:21-jdk-slim

WORKDIR /app

# Copiar el JAR desde la etapa de build
COPY --from=build /app/target/habitjourney-backend-0.0.1-SNAPSHOT.jar app.jar

# Exponer puerto de Render
EXPOSE 10000

# Ejecutar la aplicación
CMD ["java", "-jar", "app.jar"]