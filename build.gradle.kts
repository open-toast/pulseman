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

import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10"
    id("org.jetbrains.compose") version "1.1.1"
    id("org.jlleitschuh.gradle.ktlint") version "10.1.0"
}

val appVersion = "1.1.0"

group = "com.toasttab.pulseman"
version = appVersion

repositories {
    jcenter()
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

dependencies {
    implementation(compose.desktop.currentOs)

    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.fifesoft:rsyntaxtextarea:$rsyntaxVersion")
    implementation("com.google.api.grpc:proto-google-common-protos:$googleCommonProtos")
    implementation("com.google.protobuf:protobuf-java:$protobufUtils")
    implementation("com.google.protobuf:protobuf-java-util:$protobufUtils")
    implementation("com.toasttab.protokt:protokt-core:$protoktVersion")
    implementation("com.toasttab.protokt:protokt-extensions:$protoktVersion")
    implementation("org.apache.pulsar:pulsar-client-admin:$pulsarVersion")
    /**
     * Directly importing the pulsar-client jar as it is causing an issue with signing mac apps, something to do with
     * the compressed size being mismatched. They must be somehow modifying the jar before publishing.
     *  Cause: invalid entry compressed size (expected 5232 but got 5227 bytes)
     * Directly adding the jar to the project as a workaround for now. I stripped all the meta info manually to make it
     * work. Stripping all META-INF from the import may resolve it.
     * This happened with multiple versions of the import.
     */
    // implementation("org.apache.pulsar:pulsar-client:$pulsarVersion")
    implementation(files("pulsar-client-2.9.0.jar"))

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
        jvmTarget = JavaVersion.VERSION_15.toString()
        allWarningsAsErrors = true
    }
}

kotlin.sourceSets.all {
    languageSettings.optIn("kotlin.RequiresOptIn")
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
                bundleID = "com.toasttab.pulseman"
                signing {
                    sign.set(System.getenv("SIGN_APP")?.toString()?.toBoolean() ?: false)
                    identity.set(System.getenv("IDENTITY") ?: "")
                }
                notarization {
                    appleID.set(System.getenv("APPLE_ID") ?: "")
                    password.set(System.getenv("NOTARIZATION_PASSWORD") ?: "")
                    ascProvider.set(System.getenv("PROVIDER") ?: "")
                }
            }
        }
    }
}
