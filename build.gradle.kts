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
    implementation("io.vertx:vertx-web-client:$vertxVersion")
    implementation("io.vertx:vertx-codegen:$vertxVersion")
    implementation("io.vertx:vertx-service-proxy:$vertxVersion")
    implementation("io.vertx:vertx-infinispan:$vertxVersion")

    implementation("io.vertx:vertx-rx-java3:$vertxVersion")
    annotationProcessor("io.vertx:vertx-rx-java3-gen:$vertxVersion")
    implementation("ch.qos.logback:logback-classic:1.4.4")

    annotationProcessor("io.vertx:vertx-service-proxy:$vertxVersion")
    annotationProcessor("io.vertx:vertx-codegen:$vertxVersion:processor")

    val junit5Version = "5.9.1"
    testImplementation ("io.vertx:vertx-junit5:$vertxVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junit5Version")
    testImplementation ("org.assertj:assertj-core:3.11.1")
    testRuntimeOnly ("org.junit.jupiter:junit-jupiter-engine:$junit5Version")

}

java {
    sourceCompatibility = JavaVersion.VERSION_13
}

tasks.getByName<JavaCompile>("compileJava") {
    // This allows you to customize where the files are being generated.
    options.annotationProcessorGeneratedSourcesDirectory =
        File("$projectDir/src/main/generated")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
