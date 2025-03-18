/*
 * Copyright 2025 Karma Krafts & associates
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import dev.karmakrafts.conventions.GitLabCI
import java.time.ZonedDateTime

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.dokka)
    `maven-publish`
}

kotlin {
    mingwX64()
    linuxX64()
    linuxArm64()
    macosX64()
    macosArm64()
    androidNativeArm32()
    androidNativeArm64()
    androidNativeX64()
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    jvm()
    js {
        browser()
        nodejs()
    }
    applyDefaultHierarchyTemplate()
    withSourcesJar(true)
    sourceSets {
        jsMain {
            dependencies {
                implementation(libs.kotlin.wrappers.js)
                implementation(libs.kotlin.wrappers.browser)
                implementation(libs.kotlin.wrappers.browser.js)
                implementation(libs.kotlin.wrappers.web)
                implementation(libs.kotlin.wrappers.web.js)
            }
        }
    }
}

dokka {
    moduleName = project.name
    pluginsConfiguration {
        html {
            footerMessage = "(c) ${ZonedDateTime.now().year} Karma Krafts & associates"
        }
    }
}

val dokkaJar by tasks.registering(Jar::class) {
    dependsOn(tasks.dokkaGeneratePublicationHtml)
    from(tasks.dokkaGeneratePublicationHtml.flatMap { it.outputDirectory })
    archiveClassifier.set("javadoc")
}

tasks {
    System.getProperty("publishDocs.root")?.let { docsDir ->
        register("publishDocs", Copy::class) {
            dependsOn(dokkaJar)
            mustRunAfter(dokkaJar)
            from(zipTree(dokkaJar.get().outputs.files.first()))
            into(docsDir)
        }
    }
}

publishing {
    repositories {
        with(GitLabCI) { authenticatedPackageRegistry() }
    }
    publications.configureEach {
        if (this is MavenPublication) {
            artifact(dokkaJar)
            pom {
                name = project.name
                description = "Lightweight logging framework for Kotlin Multiplatform."
                url = System.getenv("CI_PROJECT_URL")
                licenses {
                    license {
                        name = "Apache License 2.0"
                        url = "https://www.apache.org/licenses/LICENSE-2.0"
                    }
                }
                developers {
                    developer {
                        id = "kitsunealex"
                        name = "KitsuneAlex"
                        url = "https://git.karmakrafts.dev/KitsuneAlex"
                    }
                }
                scm {
                    url = this@pom.url
                }
            }
        }
    }
}