package dev.karmakrafts.rakii

/**
 * A marker annotation for APIs in the RAkII resource management system that are
 * intended to be generated or transformed by the compiler plugin.
 */
@RequiresOptIn("The API you're trying to use shouldn't be used without @SkipDropTransforms")
@Retention(AnnotationRetention.BINARY)
annotation class GeneratedDropApi
