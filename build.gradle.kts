plugins {
    java
}

project.apply {
    group = "org.example"
    version = "1.0"
}

repositories {
    mavenCentral()
}

dependencies {
    val vertxVersion = "4.3.7"

    implementation("io.vertx:vertx-core:$vertxVersion")
    implementation("io.vertx:vertx-web:$vertxVersion")
    implementation("io.vertx:vertx-codegen:$vertxVersion")
    implementation("io.vertx:vertx-service-proxy:$vertxVersion")

    implementation("ch.qos.logback:logback-classic:1.4.4")
    implementation("io.vertx:vertx-rx-java3:$vertxVersion")

    annotationProcessor("io.vertx:vertx-service-proxy:$vertxVersion")
    annotationProcessor("io.vertx:vertx-codegen:$vertxVersion:processor")
}

java {
    sourceCompatibility = JavaVersion.VERSION_13
}

tasks.getByName<JavaCompile>("compileJava") {
    // This allows you to customize where the files are being generated.
    options.annotationProcessorGeneratedSourcesDirectory =
        File("$projectDir/src/main/generated")
}
