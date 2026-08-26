# ── Stage 1: Build ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:25-jdk-jammy AS builder

WORKDIR /workspace

# Copy Gradle wrapper and build files first for layer caching
COPY gradlew gradlew.bat ./
COPY gradle ./gradle
COPY build.gradle settings.gradle ./

# Copy all sub-module build files (for dependency resolution caching)
COPY atlashub-shared-kernel/build.gradle         atlashub-shared-kernel/
COPY atlashub-identity/build.gradle              atlashub-identity/
COPY atlashub-accounts/build.gradle              atlashub-accounts/
COPY atlashub-ledger/build.gradle                atlashub-ledger/
COPY atlashub-transfers/build.gradle             atlashub-transfers/
COPY atlashub-charges/build.gradle               atlashub-charges/
COPY atlashub-subscriptions/build.gradle         atlashub-subscriptions/
COPY atlashub-escrow/build.gradle                atlashub-escrow/
COPY atlashub-settlement/build.gradle            atlashub-settlement/
COPY atlashub-transaction-splits/build.gradle    atlashub-transaction-splits/
COPY atlashub-transactions-query/build.gradle    atlashub-transactions-query/
COPY atlashub-notifications/build.gradle         atlashub-notifications/
COPY atlashub-rate-limiter/build.gradle          atlashub-rate-limiter/
COPY atlashub-eventbus/build.gradle              atlashub-eventbus/
COPY atlashub-app/build.gradle                   atlashub-app/
COPY atlashub-admin/build.gradle               atlashub-admin/
COPY atlashub-auth/build.gradle                atlashub-auth/
COPY atlashub-audit/build.gradle               atlashub-audit/

# Download dependencies (cached unless build files change)
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon --quiet || true

# Copy all source code
COPY . .

# Build the fat jar — skip tests in Docker build (tests run in CI separately)
RUN ./gradlew :atlashub-app:bootJar --no-daemon -x test

# ── Stage 2: Extract layers for efficient layer caching ───────────────────────
FROM eclipse-temurin:25-jre-jammy AS extractor

WORKDIR /workspace
COPY --from=builder /workspace/atlashub-app/build/libs/atlashub.jar atlashub.jar

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

ENV OTEL_OPTS="-javaagent:/app/otel-agent.jar \
               -Dotel.service.name=atlashub \
               -Dotel.exporter.otlp.endpoint=${OTEL_EXPORTER_ENDPOINT:-http://tempo:4317} \
               -Dotel.traces.exporter=${OTEL_EXPORTER:-none}"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS $OTEL_OPTS org.springframework.boot.loader.launch.JarLauncher"]

