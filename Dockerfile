# ---- Build Stage ----
FROM public.ecr.aws/docker/library/gradle:8.7.0-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle bootJar --no-daemon

# ---- Run Stage ----
FROM public.ecr.aws/docker/library/eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
