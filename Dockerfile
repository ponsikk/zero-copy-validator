# ========================================
# Stage 1: Rust Builder
# ========================================
FROM rust:1.92-slim as rust-builder

WORKDIR /build

# Copy Rust project
COPY rust-ffi/Cargo.toml rust-ffi/Cargo.lock* ./rust-ffi/
COPY rust-ffi/src ./rust-ffi/src
COPY rust-ffi/benches ./rust-ffi/benches

# Build ONLY the library (skip benches/bins) in release mode
WORKDIR /build/rust-ffi
RUN cargo build --lib --release

# Verify the library was built
RUN ls -la target/release/ && \
    test -f target/release/libjson_validator_ffi.so

# ========================================
# Stage 2: Java Builder
# ========================================
FROM maven:3.9-eclipse-temurin-25 as java-builder

WORKDIR /build

# Copy Maven files first (for layer caching)
COPY pom.xml ./
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Copy Rust library from previous stage
COPY --from=rust-builder /build/rust-ffi/target/release/libjson_validator_ffi.so \
    src/main/resources/native/libjson_validator_ffi.so

# Build Java project
RUN mvn clean package -DskipTests

# Verify JAR was built
RUN ls -la target/ && \
    test -f target/json-validator-*.jar

# ========================================
# Stage 3: Runtime
# ========================================
FROM eclipse-temurin:25-jre-jammy

WORKDIR /app

# Copy JAR from builder
COPY --from=java-builder /build/target/json-validator-*.jar app.jar

# Copy native library
COPY --from=rust-builder /build/rust-ffi/target/release/libjson_validator_ffi.so \
    /app/native/libjson_validator_ffi.so

# Set library path
ENV LD_LIBRARY_PATH=/app/native:$LD_LIBRARY_PATH

# Expose port if needed (for future HTTP API)
EXPOSE 8080

# Run with Panama FFI enabled
ENTRYPOINT ["java", \
    "--enable-preview", \
    "--enable-native-access=ALL-UNNAMED", \
    "-Djava.library.path=/app/native", \
    "-jar", "app.jar"]
