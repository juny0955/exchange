import org.gradle.kotlin.dsl.`java-library`

plugins {
    id("com.google.protobuf") version "0.9.4"
    `java-library`
}

dependencies {
    api("io.grpc:grpc-stub:1.68.0")
    api("io.grpc:grpc-protobuf:1.68.0")
    api("com.google.protobuf:protobuf-java:3.25.5")
    compileOnly("org.apache.tomcat:annotations-api:6.0.53")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.25.5"
    }
    plugins {
        create("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.68.0"
        }
    }
    generateProtoTasks {
        all().forEach { task ->
            task.plugins.create("grpc")
        }
    }
}