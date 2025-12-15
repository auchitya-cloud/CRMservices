FROM openjdk:17-jre-slim

WORKDIR /app

COPY target/order-service-1.0.0.jar app.jar

EXPOSE 8080

# Add OpenTelemetry Java agent
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar /app/opentelemetry-javaagent.jar

ENV JAVA_OPTS="-javaagent:/app/opentelemetry-javaagent.jar"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]