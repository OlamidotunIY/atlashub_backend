# ── Stage 1: Build ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:25-jdk-jammy AS builder

WORKDIR /workspace

# Copy Gradle wrapper and build files first for layer caching
COPY gradlew gradlew.bat ./
COPY gradle ./gradle
COPY build.gradle settings.gradle ./

# Copy all actual sub-module build files (for dependency resolution caching)
COPY atlashub-shared/build.gradle               atlashub-shared/
COPY atlashub-platform/catalog/build.gradle     atlashub-platform/catalog/
COPY atlashub-platform/accounts/build.gradle    atlashub-platform/accounts/
COPY atlashub-platform/authentication/build.gradle atlashub-platform/authentication/
COPY atlashub-platform/storage/build.gradle     atlashub-platform/storage/
COPY atlashub-platform/iam/build.gradle         atlashub-platform/iam/
COPY atlashub-infrastructure/eventbus/build.gradle atlashub-infrastructure/eventbus/
COPY atlashub-infrastructure/rate-limiter/build.gradle atlashub-infrastructure/rate-limiter/
COPY atlashub-infrastructure/audit/build.gradle atlashub-infrastructure/audit/
COPY atlashub-main/build.gradle                 atlashub-main/

# Download dependencies (cached unless build files change)
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon --quiet || true

# Copy all source code
COPY . .

# Build the fat jar — skip tests in Docker build (tests run in CI separately)
RUN ./gradlew :atlashub-main:bootJar --no-daemon -x test

# ── Stage 2: Extract layers for efficient layer caching ───────────────────────
FROM eclipse-temurin:25-jre-jammy AS extractor

WORKDIR /workspace
# Copy the built jar from atlashub-main (glob match since version might change)
COPY --from=builder /workspace/atlashub-main/build/libs/*-SNAPSHOT.jar atlashub.jar

# Spring Boot layer extraction for optimal Docker caching
RUN java -Djarmode=layertools -jar atlashub.jar extract

# ── Stage 3: Final minimal runtime image ──────────────────────────────────────
FROM eclipse-temurin:25-jre-jammy AS runtime

# Security: non-root user
RUN groupadd -r atlashub && useradd -r -g atlashub atlashub

WORKDIR /app

# Copy extracted layers (ordered by change frequency: dependencies rarely change)
COPY --from=extractor /workspace/dependencies/ ./
COPY --from=extractor /workspace/spring-boot-loader/ ./
COPY --from=extractor /workspace/snapshot-dependencies/ ./
COPY --from=extractor /workspace/application/ ./

# OpenTelemetry Java agent for distributed tracing (downloaded at build time)
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v2.12.0/opentelemetry-javaagent.jar /app/otel-agent.jar

RUN chown -R atlashub:atlashub /app
USER atlashub

EXPOSE 8080
EXPOSE 8081

# Health check using Actuator
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -Djava.security.egd=file:/dev/./urandom"

ENV OTEL_SERVICE_NAME="atlashub"
ENV OTEL_OPTS="-javaagent:/app/otel-agent.jar"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS $OTEL_OPTS org.springframework.boot.loader.launch.JarLauncher"]

