# RAkII

RAkII (or Kotlin RAII) is a lightweight runtime library and compiler plugin
which allows using structured RAII with support for error handling in Kotlin Multiplatform.

RAII is a concept commonly known from native languages such as C++ and Rust,
used for managing the lifetime of memory implicitly.  
However, since Cleaners are not suitable for managing micro-allocations in 
a granular manner in Kotlin, this library can be employed to reduce potential for
common errors, resource leaks and double-frees.

### How to configure it

Simply add the required repositories to your build configuration, apply the 
Gradle plugin and add a dependency on the runtime:

In your `settings.gradle.kts`:

```kotlin
pluginManagement {
    repositories {
        // Snapshots are available from the Karma Krafts repository or Maven Central Snapshots
        maven("https://files.karmakrafts.dev/maven")
        maven("https://central.sonatype.com/repository/maven-snapshots")
        // Releases are mirrored to the central M2 repository
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        // Snapshots are available from the Karma Krafts repository or Maven Central Snapshots
        maven("https://files.karmakrafts.dev/maven")
        maven("https://central.sonatype.com/repository/maven-snapshots")
        // Releases are mirrored to the central M2 repository
        mavenCentral()
    }
}
```

In your `build.gradle.kts`::

```kotlin
plugins {
    id("dev.karmakrafts.rakii.rakii-gradle-plugin") version "<version>"
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation("dev.karmakrafts.rakii:rakii-runtime:<version>")
            }
        }
    }
}
```

**Note: It is recommended to use Gradle version catalogs which were omitted here for simplification.**

### How to use it

```kotlin
import dev.karmakrafts.rakii.Drop

class Foo : Drop {
    fun doTheThing() { 
        println("I am doing the thing!")
    }
}

class Bar : Drop {
    val foo1 by dropping(::Foo)
    // When the initialization of foo2 fails, foo1 will be dropped immediately
    val foo2 by dropping(::Foo).dropOnAnyError(Bar::foo1)
    
    fun doTheThings() {
        foo1.doTheThing()
        foo2.doTheThing()
    }
}

fun main() {
    Bar().use {
        // Normally use Bar and its contained Foo instance
        // Once the use-scope ends, Foo is dropped and Bar is dropped afterward
        it.doTheThings()
    }
}
```