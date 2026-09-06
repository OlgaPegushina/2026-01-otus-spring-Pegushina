# 1- build jar
FROM maven:3.9.3-eclipse-temurin-21 as builder

WORKDIR /app

# Копируем pom и скачиваем зависимости
COPY hw17-docker/pom.xml .
RUN mvn dependency:go-offline

# Копируем весь код и билдим jar
COPY hw17-docker/src ./src
RUN mvn clean package -DskipTests

# 2- runtime
FROM eclipse-temurin:21-jre-jammy

ENV TZ=Europe/Moscow
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

VOLUME /tmp

# Копируем из builder'а готовый jar
COPY --from=builder /app/target/*.jar app.jar

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app.jar"]