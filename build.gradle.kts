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
    val vertxVersion = "4.3.6"

    implementation("io.vertx:vertx-core:$vertxVersion")
    implementation("io.vertx:vertx-web:$vertxVersion")
    implementation("ch.qos.logback:logback-classic:1.4.4")
}

java {
    sourceCompatibility = JavaVersion.VERSION_13
}
