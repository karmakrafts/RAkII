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
import dev.karmakrafts.conventions.configureJava
import dev.karmakrafts.conventions.defaultDependencyLocking
import dev.karmakrafts.conventions.setProjectInfo
import dev.karmakrafts.conventions.signPublications
import java.net.URI
import kotlin.io.encoding.ExperimentalEncodingApi

plugins {
    alias(libs.plugins.dokka) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.kapt) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.karmaConventions)
    signing
    `maven-publish`
    alias(libs.plugins.gradleNexus)
}

group = "dev.karmakrafts.rakii"
version = GitLabCI.getDefaultVersion(libs.versions.rakii)

allprojects {
    configureJava(rootProject.libs.versions.java)
}

@OptIn(ExperimentalEncodingApi::class)
subprojects {
    apply<MavenPublishPlugin>()
    apply<SigningPlugin>()

    group = rootProject.group
    version = rootProject.version
    if (GitLabCI.isCI) defaultDependencyLocking()

    publishing {
        setProjectInfo(rootProject.name, "Structured RAII with error handling for Kotlin Multiplatform")
        with(GitLabCI) { karmaKraftsDefaults() }
    }

    signing {
        signPublications()
    }
}

nexusPublishing {
    repositories {
        System.getenv("OSSRH_USERNAME")?.let { userName ->
            sonatype {
                nexusUrl = URI.create("https://central.sonatype.com/publish/staging/maven2")
                snapshotRepositoryUrl = URI.create("https://central.sonatype.com/repository/maven-snapshots")
                username = userName
                password = System.getenv("OSSRH_PASSWORD")
            }
        }
    }
}