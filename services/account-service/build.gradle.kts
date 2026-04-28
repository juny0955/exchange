import org.jooq.meta.jaxb.Logging
import org.jooq.meta.jaxb.Property
import org.jooq.meta.jaxb.ForcedType

plugins {
    java
    id("org.springframework.boot") version "4.0.5"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.jooq.jooq-codegen-gradle") version "3.19.30"
}
val springGrpcVersion by extra("1.0.2")

description = "account-service"

val jooqGeneratedDir = layout.buildDirectory.dir("generated-src/jooq/main")

dependencies {
    implementation(project(":libs:core-web"))
    implementation(project(":libs:proto"))
    implementation("org.springframework.boot:spring-boot-starter-jooq")
    implementation("org.springframework.grpc:spring-grpc-client-spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-kafka")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    runtimeOnly("org.postgresql:postgresql")
    testCompileOnly("org.projectlombok:lombok")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testAnnotationProcessor("org.projectlombok:lombok")

    jooqCodegen("org.jooq:jooq-meta-extensions")
}

jooq {
    configuration {
        logging = Logging.WARN

        generator {
            database {
                name = "org.jooq.meta.extensions.ddl.DDLDatabase"
                inputSchema = "PUBLIC"

                properties.addAll(
                    listOf(
                        Property().withKey("scripts").withValue("src/main/resources/db/schema.sql"),
                        Property().withKey("sort").withValue("semantic"),
                        Property().withKey("unqualifiedSchema").withValue("none"),
                        Property().withKey("defaultNameCase").withValue("lower")
                    )
                )

                forcedTypes.add(
                    ForcedType()
                        .withName("INSTANT")
                        .withIncludeTypes("TIMESTAMP\\s+WITH\\s+TIME\\s+ZONE")
                )
            }

            target {
                packageName = "dev.junyoung.exchange.accountservice"
                directory = jooqGeneratedDir.get().asFile.absolutePath
            }
        }
    }
}

sourceSets {
    main {
        java.srcDir(jooqGeneratedDir)
    }
}


tasks.named("compileJava") {
    dependsOn("jooqCodegen")
}


tasks.register("prepareKotlinBuildScriptModel"){}
dependencyManagement {
    imports {
        mavenBom("org.springframework.grpc:spring-grpc-dependencies:$springGrpcVersion")
    }
}
