FROM eclipse-temurin:17-jdk-jammy AS build

ARG MODULE
WORKDIR /workspace
COPY . .
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw \
    && ./mvnw -pl "${MODULE}" -am package -DskipTests

FROM eclipse-temurin:17-jre-jammy

ARG MODULE
WORKDIR /app
COPY --from=build "/workspace/${MODULE}/target/${MODULE}-1.0.0.jar" app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
