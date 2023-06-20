/*
 * Copyright (c) 2021 Toast Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.20"
    id("org.jetbrains.compose") version "1.4.0"
    id("org.jlleitschuh.gradle.ktlint") version "11.3.2"
}

val appVersion = "1.3.0"

group = "com.toasttab.pulseman"
version = appVersion

repositories {
    mavenCentral()
    maven { url = uri("https://maven.google.com") }
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
}

val assertJVersion: String by rootProject
val composeVersion: String by rootProject
val googleCommonProtos: String by rootProject
val jacksonVersion: String by rootProject
val jUnitVersion: String by rootProject
val kotlinVersion: String by rootProject
val mockkVersion: String by rootProject
val protobufUtils: String by rootProject
val protoktVersion: String by rootProject
val pulsarVersion: String by rootProject
val reflectionsVersion: String by rootProject
val rsyntaxVersion: String by rootProject
val sl4jNoop: String by rootProject
val testContainerPulsar: String by rootProject

configurations {
    compileOnly {
        isCanBeResolved = true
    }
}

val tempBuildDir = "$buildDir/temp/"
val pulsarClientJar = "pulsar-client-$pulsarVersion.jar"

dependencies {
    implementation(compose.desktop.currentOs)

    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.fifesoft:rsyntaxtextarea:$rsyntaxVersion")
    implementation("com.google.protobuf:protobuf-java:$protobufUtils")
    implementation("com.google.protobuf:protobuf-java-util:$protobufUtils")
    implementation("com.google.protobuf:protobuf-kotlin:$protobufUtils")
    implementation("com.toasttab.protokt:protokt-core:$protoktVersion")
    implementation("com.toasttab.protokt:protokt-extensions:$protoktVersion")
    implementation("org.apache.pulsar:pulsar-client-admin:$pulsarVersion")
    implementation("org.jetbrains.compose.material:material-icons-extended:$composeVersion")
    implementation("org.jetbrains.kotlin:kotlin-compiler-embeddable:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-script-runtime:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-script-util:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-scripting-compiler-embeddable:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-scripting-jsr223:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-scripting-jvm-host:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-scripting-jvm:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    implementation("org.reflections:reflections:$reflectionsVersion")
    implementation("org.slf4j:slf4j-nop:$sl4jNoop")

    /**
     * The pulsar-client jar is causing an issue with signing Mac apps, something to do with
     * the compressed size being mismatched.
     *  Cause: invalid entry compressed size (expected 5232 but got 5227 bytes)
     * Stripping all META-INF from the import via gradle task for now to make it work.
     * This happened with multiple versions of the import.
     *
     * STRIP META-INF Part 1: Download the jar but don't include it in the final app.
     */
    compileOnly("org.apache.pulsar:pulsar-client:$pulsarVersion")

    /**
     * The protokt version of proto-google-common-protos uses the same package and class names.
     * They are only being downloaded and packaged with the release they will not be automatically loaded into the
     * classloader.
     * They will be added to separate classloaders one for protoKT protos and one for standard protos.
     */
    compileOnly("com.google.api.grpc:proto-google-common-protos:$googleCommonProtos")
    compileOnly("com.toasttab.protokt.thirdparty:proto-google-common-protos:$protoktVersion")

    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("org.apache.pulsar:pulsar-client-admin-original:$pulsarVersion")
    testImplementation("org.assertj:assertj-core:$assertJVersion")
    testImplementation("org.junit.jupiter:junit-jupiter:$jUnitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$jUnitVersion")
    testImplementation("org.testcontainers:pulsar:$testContainerPulsar")
}

tasks {
    test {
        exclude("**/*IT*.class")
        useJUnitPlatform()
    }
}

task<Test>("iTest") {
    group = "verification"
    useJUnitPlatform()
}

ktlint {
    verbose.set(true)
    outputToConsole.set(true)
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
        allWarningsAsErrors = true
    }
}

kotlin.sourceSets.all {
    languageSettings.optIn("kotlin.RequiresOptIn")
}

/**
 * This will copy and rename the downloaded common protos to the resource folder, so they can be loaded to custom
 * Jar Loaders at runtime
 */
val copyCommonProtoJarToResources by tasks.creating(Copy::class) {
    into("src/main/resources")
    val filteredFiles = configurations.compileOnly
        .get()
        .filter { it.name.startsWith("proto-google-common-protos") }
    from(filteredFiles)
    filteredFiles.forEach { file ->
        val originalFileName = file.name
        val newFileName = if (originalFileName.endsWith("$googleCommonProtos.jar")) {
            originalFileName.replace("$googleCommonProtos", "original")
        } else {
            originalFileName.replace("$protoktVersion", "protoKT")
        }
        rename(originalFileName, newFileName)
    }
}

/**
 * STRIP META-INF Part 2: Copy the jar to a temp directory
 */
val copyPulsarClientTask by tasks.creating(Copy::class) {
    into("$buildDir/temp")
    from(configurations.compileOnly.get().find { it.name.equals(pulsarClientJar) })
}

/**
 * STRIP META-INF Part 3: Strip the META-INF from the pulsar client jar.
 */
val stripMetaInfTask: TaskProvider<Task> = tasks.register("stripMetaInf") {
    dependsOn(copyPulsarClientTask)

    doLast {
        val jarFile = file("$tempBuildDir$pulsarClientJar")
        exec {
            commandLine("zip", "-d", jarFile.absolutePath, "META-INF/*")
        }
    }
}

/**
 * STRIP META-INF Part 4: Make the stripped jar an implementation dependency on the app.
 */
val importPulsarClientTask: TaskProvider<Task> = tasks.register("importPulsarClientTask") {
    dependsOn(stripMetaInfTask)

    doLast {
        dependencies {
            implementation(files("$tempBuildDir$pulsarClientJar"))
        }
    }
}

tasks.named("processResources") {
    dependsOn(copyCommonProtoJarToResources)
}

/**
 * STRIP META-INF Part 0: Kick off flow
 */
tasks.named("assemble") {
    dependsOn(importPulsarClientTask)
}

compose.desktop {
    application {
        mainClass = "com.toasttab.pulseman.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Pulseman"
            packageVersion = appVersion
            description = "Send and receive messages with pulsar"
            copyright = "Copyright (c) 2021 Toast Inc"
            includeAllModules = true

            macOS {
                iconFile.set(project.file("pulse.icns"))
                bundleID = "com.tempaccount.pulseman"
                signing {
                    sign.set(System.getenv("SIGN_APP")?.toString()?.toBoolean() ?: false)
                    identity.set(System.getenv("IDENTITY_TEMP") ?: "")
                }
                notarization {
                    appleID.set(System.getenv("APPLE_ID_TEMP") ?: "")
                    password.set(System.getenv("NOTARIZATION_PASSWORD_TEMP") ?: "")
                    ascProvider.set(System.getenv("PROVIDER_TEMP") ?: "")
                }
            }
        }
    }
}
