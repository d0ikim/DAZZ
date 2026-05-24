plugins {
    java
    id("org.springframework.boot") version "3.4.5"
}

group = "com.dazz"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    // BOM — implementation / annotationProcessor / test 각각 적용 필요 (설정 간 상속 없음)
    implementation(platform("org.springframework.boot:spring-boot-dependencies:3.4.5"))
    implementation(platform("org.springframework.cloud:spring-cloud-dependencies:2024.0.1"))
    annotationProcessor(platform("org.springframework.boot:spring-boot-dependencies:3.4.5"))
    testImplementation(platform("org.springframework.boot:spring-boot-dependencies:3.4.5"))
    testAnnotationProcessor(platform("org.springframework.boot:spring-boot-dependencies:3.4.5"))

    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.8")
    implementation("org.flywaydb:flyway-core")
    runtimeOnly("org.flywaydb:flyway-mysql")

    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.8")
    implementation("org.flywaydb:flyway-core")
    runtimeOnly("org.flywaydb:flyway-mysql")

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.redisson:redisson-spring-boot-starter:3.36.0")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j")
    compileOnly("org.projectlombok:lombok")
    runtimeOnly("com.mysql:mysql-connector-j")
    annotationProcessor("org.projectlombok:lombok")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:kafka")
    testImplementation("org.testcontainers:mysql")
    testImplementation("io.cucumber:cucumber-java:7.22.2")
    testImplementation("io.cucumber:cucumber-spring:7.22.2")
    testImplementation("io.cucumber:cucumber-junit-platform-engine:7.22.2")
    testImplementation("io.rest-assured:rest-assured:5.5.5")
    testCompileOnly("org.projectlombok:lombok")
    testImplementation("org.junit.platform:junit-platform-suite")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testAnnotationProcessor("org.projectlombok:lombok")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
