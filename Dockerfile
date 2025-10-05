FROM public.ecr.aws/docker/library/gradle:jdk21-alpine as jarBuild
WORKDIR /src

COPY . .

RUN gradle bootJar --no-daemon
RUN cp ./build/libs/belimang-0.0.1-SNAPSHOT.jar ./coffeeteam-belimang.jar

FROM public.ecr.aws/docker/library/eclipse-temurin:21-jdk-alpine as launch
WORKDIR /app
COPY --from=jarBuild /src/coffeeteam-belimang.jar coffeeteam-belimang.jar

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

CMD ["java", "-jar", "coffeeteam-belimang.jar", "$ARGS"]