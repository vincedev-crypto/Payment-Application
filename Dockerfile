# Stage 1: Build the application (Compilation Environment)
# Using a standard, available Maven image for Java 17
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# 1. COPY POM: Caches dependencies efficiently.
COPY pom.xml .

# 2. DOWNLOAD DEPENDENCIES ONLY: Resolves dependencies based on pom.xml but skips the main compile.
# The 'install' goal fails because classes don't exist yet, so we only run 'dependency:resolve' now.
RUN mvn dependency:resolve -DskipTests

# 3. COPY SOURCE CODE: This MUST happen before compilation. (THE CRITICAL FIX!)
COPY src ./src

# 4. RUN FINAL COMPILE AND PACKAGE: Compiles source code and creates the final JAR.
# Now that 'src' is present, the final packaging will succeed.
RUN mvn clean package -DskipTests

# Stage 2: Create the final image (Runtime Environment)
# Using a small, reliable JRE image (corrected tag)
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the built JAR from the previous stage (the final packaged artifact)
COPY --from=build /app/target/payment-0.0.1-SNAPSHOT.jar .

# Expose the correct port
EXPOSE 8080

# Command to run the executable JAR
ENTRYPOINT ["java","-jar","/app/payment-0.0.1-SNAPSHOT.jar"]