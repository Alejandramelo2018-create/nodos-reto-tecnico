FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw
RUN ./mvnw -q -DskipTests dependency:go-offline

COPY src ./src
RUN ./mvnw -q -DskipTests package

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

COPY --from=build /workspace/target/*.jar /app/app.jar

EXPOSE 8081
ENV SERVER_PORT=8081

# Ajustado para caber en el plan free de Render (512MB RAM / 0.1 CPU):
# heap y metaspace acotados, GC serial (menor overhead que G1 con heaps chicos)
# y menos hilos precompilados en el JIT para acelerar el arranque.
ENTRYPOINT ["java", \
    "-XX:MaxRAMPercentage=70.0", \
    "-XX:MaxMetaspaceSize=160m", \
    "-XX:ReservedCodeCacheSize=48m", \
    "-Xss256k", \
    "-XX:+UseSerialGC", \
    "-XX:TieredStopAtLevel=1", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "/app/app.jar"]