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
  implementation(platform("io.vertx:vertx-stack-depchain:4.4.0"))
  implementation("io.vertx:vertx-core")
  implementation("io.vertx:vertx-web")
  implementation("io.vertx:vertx-web-client")
  implementation("com.github.ben-manes.caffeine:caffeine:3.1.5")
}

application {
  mainClassName = "io.vertx.howtos.caffeine.CatsVerticle"
}

tasks.wrapper {
  gradleVersion = "7.4.1"
}
