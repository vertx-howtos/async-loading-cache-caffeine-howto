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
  implementation(platform("io.vertx:vertx-stack-depchain:5.0.0.CR2"))
  implementation("io.vertx:vertx-core")
  implementation("io.vertx:vertx-web")
  implementation("io.vertx:vertx-web-client")
  implementation("com.github.ben-manes.caffeine:caffeine:3.1.5")
}

application {
  mainClass = "io.vertx.howtos.caffeine.CatsVerticle"
}
