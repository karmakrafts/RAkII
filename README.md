# RAkII

[![](https://git.karmakrafts.dev/kk/rakii/badges/master/pipeline.svg)](https://git.karmakrafts.dev/kk/rakii/-/pipelines)
[![](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Frepo.maven.apache.org%2Fmaven2%2Fdev%2Fkarmakrafts%2Frakii%2Frakii-runtime%2Fmaven-metadata.xml
)](https://git.karmakrafts.dev/kk/rakii/-/packages)
[![](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fcentral.sonatype.com%2Frepository%2Fmaven-snapshots%2Fdev%2Fkarmakrafts%2Frakii%2Frakii-runtime%2Fmaven-metadata.xml
)](https://git.karmakrafts.dev/kk/rakii/-/packages)

RAkII (or Kotlin RAII) is a lightweight runtime library and compiler plugin
which allows using structured RAII with support for error handling in Kotlin Multiplatform.

RAII is a concept commonly known from native languages such as C++ and Rust,
used for managing the lifetime of memory implicitly.  
However, since Cleaners are not suitable for managing micro-allocations in
a granular manner in Kotlin, this library can be employed to reduce potential for
common errors, resource leaks and double-frees.

### How to use it

Simply add the required repositories to your build configuration, apply the
Gradle plugin and add a dependency on the runtime:

In your `settings.gradle.kts`:

```kotlin
pluginManagement {
    repositories {
        maven("https://central.sonatype.com/repository/maven-snapshots")
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        maven("https://central.sonatype.com/repository/maven-snapshots")
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

### Usage example

```kotlin
import dev.karmakrafts.rakii.Drop
import dev.karmakrafts.rakii.deferring

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
    
    fun helloWorld() = deferring {
        // Tied to the surrounding deferring scope
        val foo3 by dropping(::Foo)
        foo3.doTheThing()
    }
}

fun main() {
    Bar().use {
        // Normally use Bar and its contained Foo instance
        // Once the use-scope ends, Foo is dropped and Bar is dropped afterward
        it.doTheThings()
        it.helloWorld()
    }
}
```

### Performance implications

The RAkII compiler relies on the fact, that it can fall back to the runtime implementation  
when no optimization is applicable for a certain case.  
This means that certain usages of RAII constructs can employ a certain runtime overhead,  
which may not always be desirable.

Take the following use cases of a `deferring` scope as an example:

```kotlin
import dev.karmakrafts.rakii.deferring

fun test(closure1: () -> Unit, closure2: () -> Unit): String = deferring {
    // Can be reached with compiler optimization
    defer { println("HELLO WORLD!") }
    // Cannot be reached with compiler optimization
    defer(closure1)
    fun test2(): DropDelegate<String, DroppingScope.Owner> {
        // Cannot be reached with compiler optimization
        defer { println("HELLO WORLD!") }
        closure2()
        // Cannot be reached with compiler optimization
        return dropping(::println) { "Testing" }
    }
    // Cannot be reached with compiler optimization
    val value by test2()
    // Can be reached with compiler optimization
    val value2 by dropping(::println) { "!!!" }
    value + value2
}
```

As the above example illustrates as a fact:  
If the `DroppingScope` instance is implicitly captured, compiler optimizations may not apply.  
There is many more edge cases which are handled by the runtime due to complexity constraints in the compiler.