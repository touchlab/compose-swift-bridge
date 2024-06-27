package co.touchlab.compose.expect.swift

/**
 * Annotation used internally for the SKIE SubPlugin, please don't use it!
 */
@Target(AnnotationTarget.CLASS)
annotation class GeneratedExpectSwiftView(
    val factoryName: String,
    val type: String,
    val viewName: String,
)

