plugins {
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"
    application
}
repositories {
    mavenCentral()
    mavenLocal()
}
dependencies {
    // Ktor Server
    implementation("io.ktor:ktor-server-core:2.3.7")
    implementation("io.ktor:ktor-server-netty:2.3.7")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.7")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")
    implementation("io.ktor:ktor-server-call-logging:2.3.7")
    implementation("io.ktor:ktor-server-status-pages:2.3.7")

    // Database
    implementation("org.postgresql:postgresql:42.7.1")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("org.jetbrains.exposed:exposed-core:0.45.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.45.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.45.0")

    // Logging
    implementation("ch.qos.logback:logback-classic:1.4.14")
    implementation("io.github.oshai:kotlin-logging-jvm:5.1.0")

    // Testing
    testImplementation("io.ktor:ktor-server-tests:2.3.7")
    testImplementation("io.ktor:ktor-client-content-negotiation:2.3.7")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:1.9.22")
    testImplementation(platform("org.junit:junit-bom:5.10.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("io.ktor:ktor-client-core:2.3.7")
    testImplementation("io.ktor:ktor-client-cio:2.3.7")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
}
java { toolchain { languageVersion = JavaLanguageVersion.of(21) } }
application { mainClass = "io.nexure.discount.ApplicationKt" }

tasks {
    test {
        useJUnitPlatform()
        testLogging { events("passed", "skipped", "failed") }
    }
}
