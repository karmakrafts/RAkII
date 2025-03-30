package dev.karmakrafts.rakii

/**
 * A marker annotation for APIs that shouldn't
 * be used without marking the containing class
 * with the [SkipDropTransforms] annotation.
 */
@RequiresOptIn("The API you're trying to use shouldn't be used without @SkipDropTransforms")
@Retention(AnnotationRetention.BINARY)
annotation class GeneratedDropApi