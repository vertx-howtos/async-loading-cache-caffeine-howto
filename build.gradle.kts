plugins {
  java
  application
}

repositories {
  mavenCentral()
}

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(11))
  }
}

dependencies {
  val vertxVersion = "4.2.6"
  implementation("io.vertx:vertx-core:${vertxVersion}")
  implementation("io.vertx:vertx-web:${vertxVersion}")
  implementation("io.vertx:vertx-web-client:${vertxVersion}")
  implementation("com.github.ben-manes.caffeine:caffeine:3.0.6")
}

application {
  mainClassName = "io.vertx.howtos.caffeine.CatsVerticle"
}

tasks.wrapper {
  gradleVersion = "7.4.1"
}
